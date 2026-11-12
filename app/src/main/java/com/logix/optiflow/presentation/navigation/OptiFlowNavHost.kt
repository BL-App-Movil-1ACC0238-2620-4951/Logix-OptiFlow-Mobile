package com.logix.optiflow.presentation.navigation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.logix.optiflow.presentation.auth.UserRole
import com.logix.optiflow.presentation.auth.screens.AuthScreen
import com.logix.optiflow.presentation.booking.screens.BookingScreen
import com.logix.optiflow.presentation.notifications.screens.NotificationsScreen
import com.logix.optiflow.presentation.patient.screens.EditProfileScreen
import com.logix.optiflow.presentation.patient.screens.InfoScreen
import com.logix.optiflow.presentation.patient.screens.InfoTopic
import com.logix.optiflow.presentation.patient.screens.AppointmentDetailScreen
import com.logix.optiflow.presentation.patient.screens.CatalogScreen
import com.logix.optiflow.presentation.patient.screens.ClinicalHistoryScreen
import com.logix.optiflow.presentation.patient.screens.FrameDetailScreen
import com.logix.optiflow.presentation.patient.screens.MyAppointmentsScreen
import com.logix.optiflow.presentation.patient.screens.MyOrdersScreen
import com.logix.optiflow.presentation.patient.screens.OrderTrackingScreen
import com.logix.optiflow.presentation.patient.screens.PatientHomeScreen
import com.logix.optiflow.presentation.patient.screens.PrescriptionListScreen
import com.logix.optiflow.presentation.patient.screens.PrescriptionScreen
import com.logix.optiflow.presentation.patient.screens.ProfileScreen
import com.logix.optiflow.presentation.patient.screens.PurchasesScreen
import com.logix.optiflow.presentation.patient.screens.RescheduleScreen
import com.logix.optiflow.presentation.patient.screens.SettingsScreen
import com.logix.optiflow.presentation.patient.screens.TryOnScreen
import com.logix.optiflow.presentation.search.screens.StoreMapScreen
import com.logix.optiflow.presentation.search.screens.StoreSearchScreen
import com.logix.optiflow.presentation.staff.screens.AlertsScreen
import com.logix.optiflow.presentation.staff.screens.ClinicalCareScreen
import com.logix.optiflow.presentation.staff.screens.SaleScreen
import com.logix.optiflow.presentation.staff.screens.NewAppointmentScreen
import com.logix.optiflow.presentation.staff.screens.NewFrameScreen
import com.logix.optiflow.presentation.staff.screens.NewMovementScreen
import com.logix.optiflow.presentation.staff.screens.NewPatientScreen
import com.logix.optiflow.presentation.staff.screens.ProductDetailScreen
import com.logix.optiflow.presentation.staff.screens.ProductionScreen
import com.logix.optiflow.presentation.staff.screens.QuotePatientPickerScreen
import com.logix.optiflow.presentation.staff.screens.QuoteScreen
import com.logix.optiflow.presentation.staff.screens.ReportsScreen
import com.logix.optiflow.presentation.staff.screens.ScheduleScreen
import com.logix.optiflow.presentation.staff.screens.StaffMembersScreen
import com.logix.optiflow.presentation.staff.screens.SuppliersScreen
import com.logix.optiflow.presentation.staff.screens.ScannerScreen
import com.logix.optiflow.presentation.staff.screens.StaffAgendaScreen
import com.logix.optiflow.presentation.staff.screens.StaffDashboardScreen
import com.logix.optiflow.presentation.staff.screens.StaffInventoryScreen
import com.logix.optiflow.presentation.staff.screens.StaffMoreScreen
import com.logix.optiflow.presentation.staff.screens.StaffPatientRecordScreen
import com.logix.optiflow.presentation.staff.screens.StaffPatientsScreen
import com.logix.optiflow.presentation.staff.screens.StockManagementScreen
import com.logix.optiflow.presentation.staff.screens.WorkOrderScreen
import com.logix.optiflow.presentation.theme.LoginGradientTop

private const val SETTINGS_PATTERN = "p/settings?staff={staff}"
private const val BOOK_ARG = "storeId"

@Composable
fun OptiFlowNavHost(
    openNotifications: Boolean = false,
    onNotificationsOpened: () -> Unit = {},
    sessionViewModel: SessionViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val startDestination by sessionViewModel.startDestination.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        sessionViewModel.sessionExpired.collect {
            Toast.makeText(context, "Tu sesión expiró. Inicia sesión de nuevo.", Toast.LENGTH_LONG).show()
            navController.switchRoot(Routes.AUTH)
        }
    }
    val start = startDestination
    if (start == null) {
        Box(Modifier.fillMaxSize().background(LoginGradientTop))
        return
    }

    LaunchedEffect(openNotifications) {
        if (!openNotifications) return@LaunchedEffect
        // Solo el paciente recibe push; sin sesión de paciente se queda donde está.
        if (navController.currentDestination?.route?.startsWith("p/") == true) {
            navController.navigate(Routes.P_NOTIFICATIONS) { launchSingleTop = true }
        }
        onNotificationsOpened()
    }

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.AUTH) {
            AuthScreen(
                onAuthenticated = { role ->
                    navController.switchRoot(if (role == UserRole.CLINICAL) Routes.S_HOME else Routes.P_HOME)
                },
            )
        }

        // Paciente
        composable(Routes.P_HOME) { PatientHomeScreen(navController) }
        composable(Routes.P_NOTIFICATIONS) { NotificationsScreen(navController) }
        composable(Routes.P_SEARCH) { StoreSearchScreen(navController) }
        composable(Routes.P_STORE_MAP) { StoreMapScreen(navController) }
        composable(Routes.P_CATALOG) { CatalogScreen(navController) }
        composable(Routes.P_FRAME) { FrameDetailScreen(navController, it.arg("id")) }
        composable(Routes.P_TRYON) { TryOnScreen(navController, it.arg("id")) }
        composable(Routes.P_PRESCRIPTION) { PrescriptionScreen(navController) }
        composable(Routes.P_PRESCRIPTIONS) { PrescriptionListScreen(navController) }
        composable(Routes.P_PRESCRIPTION_DETAIL) { PrescriptionScreen(navController, recordId = it.arg("id")) }
        composable(Routes.P_APPOINTMENTS) { MyAppointmentsScreen(navController) }
        composable(Routes.P_APPOINTMENT) { AppointmentDetailScreen(navController, it.arg("id")) }
        composable(Routes.P_RESCHEDULE) { RescheduleScreen(navController, it.arg("id")) }
        composable(
            route = Routes.P_BOOK,
            arguments =
                listOf(
                    navArgument(BOOK_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
        ) { BookingScreen(navController) }
        composable(Routes.P_ORDERS) { MyOrdersScreen(navController) }
        composable(Routes.P_ORDER) { OrderTrackingScreen(navController, it.arg("id")) }
        composable(Routes.P_PROFILE) { ProfileScreen(navController) }
        composable(Routes.P_EDIT_PROFILE) { EditProfileScreen(navController) }
        composable(
            route = Routes.P_INFO,
            arguments =
                listOf(
                    navArgument("topic") { type = NavType.StringType },
                    navArgument("staff") {
                        type = NavType.BoolType
                        defaultValue = false
                    },
                ),
        ) { entry ->
            InfoScreen(
                navController,
                InfoTopic.from(entry.arguments?.getString("topic")),
                staff = entry.arguments?.getBoolean("staff") == true,
            )
        }
        composable(Routes.P_HISTORY) { ClinicalHistoryScreen(navController) }
        composable(Routes.P_PURCHASES) { PurchasesScreen(navController) }
        composable(
            route = SETTINGS_PATTERN,
            arguments =
                listOf(
                    navArgument("staff") {
                        type = NavType.BoolType
                        defaultValue = false
                    },
                ),
        ) { entry -> SettingsScreen(navController, staff = entry.arguments?.getBoolean("staff") == true) }

        // Personal clínico
        composable(Routes.S_HOME) { StaffDashboardScreen(navController) }
        composable(Routes.S_AGENDA) { StaffAgendaScreen(navController) }
        composable(Routes.S_PATIENTS) { StaffPatientsScreen(navController) }
        composable(Routes.S_INVENTORY) { StaffInventoryScreen(navController) }
        composable(Routes.S_MORE) { StaffMoreScreen(navController) }
        composable(Routes.S_NEW_PATIENT) { NewPatientScreen(navController) }
        composable(Routes.S_PATIENT) { StaffPatientRecordScreen(navController, it.arg("id")) }
        composable(
            route = Routes.S_NEW_APPOINTMENT,
            arguments =
                listOf(
                    navArgument("patientId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
        ) { NewAppointmentScreen(navController, it.arguments?.getString("patientId")) }
        composable(Routes.S_CLINICAL) { ClinicalCareScreen(navController, it.arg("patientId")) }
        composable(Routes.S_WORK_ORDER) { WorkOrderScreen(navController) }
        composable(Routes.S_PRODUCTION) { ProductionScreen(navController) }
        composable(Routes.S_QUOTE) { QuotePatientPickerScreen(navController) }
        composable(Routes.S_QUOTE_PATIENT) { QuoteScreen(navController, it.arg("patientId")) }
        composable(Routes.S_SALE) { SaleScreen(navController, it.arg("patientId")) }
        composable(Routes.S_SCANNER) { ScannerScreen(navController) }
        composable(Routes.S_PRODUCT) { ProductDetailScreen(navController) }
        composable(Routes.S_STOCK) { StockManagementScreen(navController) }
        composable(
            route = Routes.S_MOVEMENT,
            arguments =
                listOf(
                    navArgument("sku") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
        ) { NewMovementScreen(navController) }
        composable(Routes.S_NEW_FRAME) { NewFrameScreen(navController) }
        composable(Routes.S_ALERTS) { AlertsScreen(navController) }
        composable(Routes.S_REPORTS) { ReportsScreen(navController) }
        composable(Routes.S_STAFF_MEMBERS) { StaffMembersScreen(navController) }
        composable(Routes.S_SUPPLIERS) { SuppliersScreen(navController) }
        composable(Routes.S_SCHEDULE) { ScheduleScreen(navController) }
    }
}

private fun NavBackStackEntry.arg(name: String): String = arguments?.getString(name).orEmpty()
