package com.carepulse.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.carepulse.app.data.auth.AuthState
import com.carepulse.app.data.model.UserRole
import com.carepulse.app.ui.screens.activity.ActivityScreen
import com.carepulse.app.ui.screens.agency.AgencyBillingScreen
import com.carepulse.app.ui.screens.agency.AgencyCaregiversScreen
import com.carepulse.app.ui.screens.agency.AgencyDashboardScreen
import com.carepulse.app.ui.screens.agency.AgencyRequestsScreen
import com.carepulse.app.ui.screens.auth.CaregiverRegistrationScreen
import com.carepulse.app.ui.screens.auth.LoginScreen
import com.carepulse.app.ui.screens.caregiver.CaregiverDashboardScreen
import com.carepulse.app.ui.screens.caregiver.ShiftSummaryScreen
import com.carepulse.app.ui.screens.customer.BookingScreen
import com.carepulse.app.ui.screens.customer.CaregiverDetailScreen
import com.carepulse.app.ui.screens.customer.CustomerDashboardScreen
import com.carepulse.app.ui.screens.customer.PulseDashboardScreen
import com.carepulse.app.ui.screens.customer.VideoCallScreen
import com.carepulse.app.ui.screens.messages.MessagesScreen
import com.carepulse.app.ui.screens.onboarding.RoleSelectionScreen
import com.carepulse.app.ui.screens.settings.SettingsScreen
import com.carepulse.app.ui.theme.CreamBackground
import com.carepulse.app.ui.theme.InkSecondary
import com.carepulse.app.ui.theme.PastelMintDeep
import com.carepulse.app.viewmodel.CarePulseViewModel

/** Type-safe route constants for the nav graph. */
object Routes {
    const val Splash = "splash"
    const val RoleSelection = "role-selection"
    const val Login = "login/{role}"
    fun login(role: String) = "login/$role"

    const val CaregiverRegistration = "caregiver-registration"

    // --- Bottom-nav tabs ---------------------------------------------------
    const val Home = "home"
    const val Pulse = "pulse"
    const val Messages = "messages"
    const val Activity = "activity"
    const val Settings = "settings"

    // --- Full-screen (no bottom bar) ---------------------------------------
    const val CaregiverDetail = "caregiver-detail/{caregiverId}"
    fun caregiverDetail(id: String) = "caregiver-detail/$id"
    const val Booking = "booking/{caregiverId}"
    fun booking(id: String) = "booking/$id"
    const val VideoCall = "video-call"
    const val ShiftSummary = "shift-summary"
}

/** One bottom-navigation tab. */
private data class TabItem(val route: String, val label: String, val icon: ImageVector)

/** Tab labels/icons differ by role; the underlying routes stay the same. */
private fun tabsFor(role: UserRole?): List<TabItem> = when (role) {
    UserRole.AGENCY -> listOf(
        TabItem(Routes.Home, "Dashboard", Icons.Filled.Home),
        TabItem(Routes.Pulse, "Caregivers", Icons.Filled.Groups),
        TabItem(Routes.Messages, "Requests", Icons.Filled.Inbox),
        TabItem(Routes.Activity, "Billing", Icons.Filled.Payments),
        TabItem(Routes.Settings, "Settings", Icons.Filled.Settings)
    )
    else -> listOf(
        TabItem(Routes.Home, "Home", Icons.Filled.Home),
        TabItem(Routes.Pulse, "Pulse", Icons.Filled.MonitorHeart),
        TabItem(Routes.Messages, "Messages", Icons.Filled.ChatBubbleOutline),
        TabItem(Routes.Activity, "Activity", Icons.AutoMirrored.Filled.EventNote),
        TabItem(Routes.Settings, "Settings", Icons.Filled.Settings)
    )
}

private val tabRoutes = setOf(
    Routes.Home, Routes.Pulse, Routes.Messages, Routes.Activity, Routes.Settings
)

@Composable
fun CarePulseNavGraph() {
    val navController = rememberNavController()
    val vm: CarePulseViewModel = viewModel(factory = CarePulseViewModel.Factory)

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in tabRoutes
    val role by vm.role.collectAsState()

    fun clearTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
        }
    }

    Scaffold(
        containerColor = CreamBackground,
        bottomBar = {
            if (showBottomBar) {
                BottomBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    tabs = tabsFor(role)
                )
            }
        }
    ) { scaffoldPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Splash,
            modifier = Modifier.padding(scaffoldPadding)
        ) {

            // --- Session gate -------------------------------------------------
            composable(Routes.Splash) {
                val authState by vm.authState.collectAsState()
                val profile by vm.profile.collectAsState()
                LaunchedEffect(authState, profile) {
                    when (authState) {
                        is AuthState.SignedIn -> if (profile != null) clearTo(Routes.Home)
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
                        if (role == "CAREGIVER" && isNewCaregiver) clearTo(Routes.CaregiverRegistration)
                        else clearTo(Routes.Home)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.CaregiverRegistration) {
                CaregiverRegistrationScreen(
                    vm = vm,
                    onDone = { clearTo(Routes.Home) }
                )
            }

            // --- Tabs --------------------------------------------------------
            composable(Routes.Home) {
                val signOut = { vm.signOut(); clearTo(Routes.RoleSelection) }
                when (role) {
                    UserRole.AGENCY -> AgencyDashboardScreen(vm = vm)
                    UserRole.CAREGIVER -> CaregiverDashboardScreen(
                        vm = vm,
                        onClockOut = { navController.navigate(Routes.ShiftSummary) },
                        onSignOut = signOut
                    )
                    else -> CustomerDashboardScreen(
                        vm = vm,
                        onOpenCaregiver = { id -> navController.navigate(Routes.caregiverDetail(id)) },
                        onOpenPulse = { navController.navigate(Routes.Pulse) { launchSingleTop = true } },
                        onSignOut = signOut
                    )
                }
            }

            composable(Routes.Pulse) {
                if (role == UserRole.AGENCY) {
                    AgencyCaregiversScreen(vm = vm)
                } else {
                    PulseDashboardScreen(
                        vm = vm,
                        onVideoCall = { navController.navigate(Routes.VideoCall) },
                        onBack = null
                    )
                }
            }

            composable(Routes.Messages) {
                if (role == UserRole.AGENCY) AgencyRequestsScreen(vm = vm)
                else MessagesScreen(vm = vm)
            }

            composable(Routes.Activity) {
                if (role == UserRole.AGENCY) AgencyBillingScreen(vm = vm)
                else ActivityScreen(vm = vm)
            }

            composable(Routes.Settings) {
                SettingsScreen(
                    vm = vm,
                    onSignOut = { vm.signOut(); clearTo(Routes.RoleSelection) }
                )
            }

            // --- Full-screen routes -----------------------------------------
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
                    onComplete = { navController.popBackStack(Routes.Home, inclusive = false) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.VideoCall) {
                VideoCallScreen(onEndCall = { navController.popBackStack() })
            }

            composable(Routes.ShiftSummary) {
                ShiftSummaryScreen(
                    vm = vm,
                    onSubmit = { navController.popBackStack(Routes.Home, inclusive = false) },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun BottomBar(
    navController: NavHostController,
    currentRoute: String?,
    tabs: List<TabItem>
) {
    NavigationBar(containerColor = Color.White) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    if (currentRoute != tab.route) {
                        navController.navigate(tab.route) {
                            popUpTo(Routes.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PastelMintDeep,
                    selectedTextColor = PastelMintDeep,
                    unselectedIconColor = InkSecondary,
                    unselectedTextColor = InkSecondary,
                    indicatorColor = CreamBackground
                )
            )
        }
    }
}
