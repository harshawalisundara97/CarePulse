package com.carepulse.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.carepulse.app.data.auth.AuthState
import com.carepulse.app.data.model.UserRole
import com.carepulse.app.ui.screens.auth.CaregiverRegistrationScreen
import com.carepulse.app.ui.screens.auth.LoginScreen
import com.carepulse.app.ui.screens.caregiver.CaregiverDashboardScreen
import com.carepulse.app.ui.screens.caregiver.ShiftSummaryScreen
import com.carepulse.app.ui.screens.customer.BookingScreen
import com.carepulse.app.ui.screens.customer.CaregiverDetailScreen
import com.carepulse.app.ui.screens.customer.CustomerDashboardScreen
import com.carepulse.app.ui.screens.customer.PulseDashboardScreen
import com.carepulse.app.ui.screens.customer.VideoCallScreen
import com.carepulse.app.ui.screens.onboarding.RoleSelectionScreen
import com.carepulse.app.viewmodel.CarePulseViewModel

/** Type-safe route constants for the nav graph. */
object Routes {
    const val Splash = "splash"
    const val RoleSelection = "role-selection"
    const val Login = "login/{role}"
    fun login(role: String) = "login/$role"

    const val CaregiverRegistration = "caregiver-registration"
    const val CustomerDashboard = "customer-dashboard"

    const val CaregiverDetail = "caregiver-detail/{caregiverId}"
    fun caregiverDetail(id: String) = "caregiver-detail/$id"

    const val Booking = "booking/{caregiverId}"
    fun booking(id: String) = "booking/$id"

    const val PulseDashboard = "pulse-dashboard"
    const val VideoCall = "video-call"

    const val CaregiverDashboard = "caregiver-dashboard"
    const val ShiftSummary = "shift-summary"
}

@Composable
fun CarePulseNavGraph() {
    val navController = rememberNavController()
    val vm: CarePulseViewModel = viewModel(factory = CarePulseViewModel.Factory)

    fun clearTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = Routes.Splash) {

        // --- Session gate: routes signed-in users straight to their dashboard.
        composable(Routes.Splash) {
            val authState by vm.authState.collectAsState()
            val profile by vm.profile.collectAsState()
            LaunchedEffect(authState, profile) {
                when (authState) {
                    is AuthState.SignedIn -> profile?.let { p ->
                        clearTo(
                            if (p.role == UserRole.CAREGIVER) Routes.CaregiverDashboard
                            else Routes.CustomerDashboard
                        )
                    }
                    AuthState.SignedOut -> clearTo(Routes.RoleSelection)
                    AuthState.Loading -> Unit
                }
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        composable(Routes.RoleSelection) {
            RoleSelectionScreen(
                onRoleSelected = { role -> navController.navigate(Routes.login(role.name)) }
            )
        }

        composable(
            Routes.Login,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { entry ->
            val role = entry.arguments?.getString("role") ?: "CUSTOMER"
            LoginScreen(
                role = role,
                vm = vm,
                onAuthSuccess = { isNewCaregiver ->
                    val target = if (role == "CAREGIVER") {
                        if (isNewCaregiver) Routes.CaregiverRegistration else Routes.CaregiverDashboard
                    } else {
                        Routes.CustomerDashboard
                    }
                    clearTo(target)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CaregiverRegistration) {
            CaregiverRegistrationScreen(
                vm = vm,
                onDone = { clearTo(Routes.CaregiverDashboard) }
            )
        }

        composable(Routes.CustomerDashboard) {
            CustomerDashboardScreen(
                vm = vm,
                onOpenCaregiver = { id -> navController.navigate(Routes.caregiverDetail(id)) },
                onOpenPulse = { navController.navigate(Routes.PulseDashboard) },
                onSignOut = { vm.signOut(); clearTo(Routes.RoleSelection) }
            )
        }

        composable(
            Routes.CaregiverDetail,
            arguments = listOf(navArgument("caregiverId") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("caregiverId") ?: return@composable
            CaregiverDetailScreen(
                caregiverId = id,
                vm = vm,
                onBook = { navController.navigate(Routes.booking(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.Booking,
            arguments = listOf(navArgument("caregiverId") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("caregiverId") ?: return@composable
            BookingScreen(
                caregiverId = id,
                vm = vm,
                onComplete = {
                    navController.popBackStack(Routes.CustomerDashboard, inclusive = false)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PulseDashboard) {
            PulseDashboardScreen(
                vm = vm,
                onVideoCall = { navController.navigate(Routes.VideoCall) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.VideoCall) {
            VideoCallScreen(onEndCall = { navController.popBackStack() })
        }

        composable(Routes.CaregiverDashboard) {
            CaregiverDashboardScreen(
                vm = vm,
                onClockOut = { navController.navigate(Routes.ShiftSummary) },
                onSignOut = { vm.signOut(); clearTo(Routes.RoleSelection) }
            )
        }

        composable(Routes.ShiftSummary) {
            ShiftSummaryScreen(
                vm = vm,
                onSubmit = { navController.popBackStack(Routes.CaregiverDashboard, inclusive = false) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
