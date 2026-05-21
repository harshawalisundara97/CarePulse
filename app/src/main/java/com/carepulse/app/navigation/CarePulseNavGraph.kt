package com.carepulse.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
    val vm: CarePulseViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.RoleSelection) {

        composable(Routes.RoleSelection) {
            RoleSelectionScreen(
                onRoleSelected = { role ->
                    vm.setRole(role)
                    navController.navigate(Routes.login(role.name))
                }
            )
        }

        composable(
            Routes.Login,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { entry ->
            val role = entry.arguments?.getString("role") ?: "CUSTOMER"
            LoginScreen(
                role = role,
                onLogin = { name ->
                    vm.setDisplayName(name)
                    if (role == "CAREGIVER") {
                        navController.navigate(Routes.CaregiverRegistration) {
                            popUpTo(Routes.RoleSelection) { inclusive = false }
                        }
                    } else {
                        navController.navigate(Routes.CustomerDashboard) {
                            popUpTo(Routes.RoleSelection) { inclusive = false }
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CaregiverRegistration) {
            CaregiverRegistrationScreen(
                vm = vm,
                onDone = {
                    navController.navigate(Routes.CaregiverDashboard) {
                        popUpTo(Routes.RoleSelection) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CustomerDashboard) {
            CustomerDashboardScreen(
                vm = vm,
                onOpenCaregiver = { id -> navController.navigate(Routes.caregiverDetail(id)) },
                onOpenPulse = { navController.navigate(Routes.PulseDashboard) }
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
                onClockOut = { navController.navigate(Routes.ShiftSummary) }
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
