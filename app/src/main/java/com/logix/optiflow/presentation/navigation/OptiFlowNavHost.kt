package com.logix.optiflow.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.logix.optiflow.di.SearchBookingModule
import com.logix.optiflow.presentation.auth.UserRole
import com.logix.optiflow.presentation.auth.screens.AuthScreen
import com.logix.optiflow.presentation.booking.screens.BookingScreen
import com.logix.optiflow.presentation.notifications.screens.NotificationsScreen
import com.logix.optiflow.presentation.patient.screens.AppointmentDetailScreen
import com.logix.optiflow.presentation.patient.screens.CatalogScreen
import com.logix.optiflow.presentation.patient.screens.ClinicalHistoryScreen
import com.logix.optiflow.presentation.patient.screens.FrameDetailScreen
import com.logix.optiflow.presentation.patient.screens.MyAppointmentsScreen
import com.logix.optiflow.presentation.patient.screens.MyOrdersScreen
import com.logix.optiflow.presentation.patient.screens.OrderTrackingScreen
import com.logix.optiflow.presentation.patient.screens.PatientHomeScreen
import com.logix.optiflow.presentation.patient.screens.PrescriptionScreen
import com.logix.optiflow.presentation.patient.screens.ProfileScreen
import com.logix.optiflow.presentation.patient.screens.RescheduleScreen
import com.logix.optiflow.presentation.patient.screens.SettingsScreen
import com.logix.optiflow.presentation.patient.screens.TryOnScreen
import com.logix.optiflow.presentation.search.screens.StoreSearchScreen
import com.logix.optiflow.presentation.staff.screens.AlertsScreen
import com.logix.optiflow.presentation.staff.screens.ClinicalCareScreen
import com.logix.optiflow.presentation.staff.screens.ConfirmSaleScreen
import com.logix.optiflow.presentation.staff.screens.NewAppointmentScreen
import com.logix.optiflow.presentation.staff.screens.NewMovementScreen
import com.logix.optiflow.presentation.staff.screens.NewPatientScreen
import com.logix.optiflow.presentation.staff.screens.ProductDetailScreen
import com.logix.optiflow.presentation.staff.screens.ProductionScreen
import com.logix.optiflow.presentation.staff.screens.QuoteScreen
import com.logix.optiflow.presentation.staff.screens.ReportsScreen
import com.logix.optiflow.presentation.staff.screens.SaleDoneScreen
import com.logix.optiflow.presentation.staff.screens.ScannerScreen
import com.logix.optiflow.presentation.staff.screens.StaffAgendaScreen
import com.logix.optiflow.presentation.staff.screens.StaffDashboardScreen
import com.logix.optiflow.presentation.staff.screens.StaffInventoryScreen
import com.logix.optiflow.presentation.staff.screens.StaffMoreScreen
import com.logix.optiflow.presentation.staff.screens.StaffPatientRecordScreen
import com.logix.optiflow.presentation.staff.screens.StaffPatientsScreen
import com.logix.optiflow.presentation.staff.screens.StockManagementScreen
import com.logix.optiflow.presentation.staff.screens.VisualEvaluationScreen
import com.logix.optiflow.presentation.staff.screens.WorkOrderScreen
import com.logix.optiflow.presentation.theme.LoginGradientTop
import java.util.UUID

private const val SETTINGS_PATTERN = "p/settings?staff={staff}"
private const val BOOK_ARG = "storeId"

@Composable
fun OptiFlowNavHost() {
    val navController = rememberNavController()
    var start by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val session = runCatching { SearchBookingModule.getPatientSessionUseCase() }.getOrNull()
        start =
            when {
                session?.token.isNullOrBlank() -> Routes.AUTH
                session?.isPatient == false -> Routes.S_HOME
                else -> Routes.P_HOME
            }
    }

    val startDestination = start
    if (startDestination == null) {
        Box(Modifier.fillMaxSize().background(LoginGradientTop))
        return
    }

    NavHost(navController = navController, startDestination = startDestination) {
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
        composable(Routes.P_CATALOG) { CatalogScreen(navController) }
        composable(Routes.P_FRAME) { FrameDetailScreen(navController, it.arg("id")) }
        composable(Routes.P_TRYON) { TryOnScreen(navController, it.arg("id")) }
        composable(Routes.P_PRESCRIPTION) { PrescriptionScreen(navController, editable = false) }
        composable(Routes.P_PRESCRIPTION_EDIT) { PrescriptionScreen(navController, editable = true) }
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
        ) { entry ->
            val storeId = entry.arguments?.getString(BOOK_ARG)?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            BookingScreen(navController, storeId)
        }
        composable(Routes.P_ORDERS) { MyOrdersScreen(navController) }
        composable(Routes.P_ORDER) { OrderTrackingScreen(navController, it.arg("id")) }
        composable(Routes.P_PROFILE) { ProfileScreen(navController) }
        composable(Routes.P_HISTORY) { ClinicalHistoryScreen(navController) }
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
        composable(Routes.S_NEW_APPOINTMENT) { NewAppointmentScreen(navController) }
        composable(Routes.S_CLINICAL) { ClinicalCareScreen(navController) }
        composable(Routes.S_EVALUATION) { VisualEvaluationScreen(navController) }
        composable(Routes.S_WORK_ORDER) { WorkOrderScreen(navController) }
        composable(Routes.S_PRODUCTION) { ProductionScreen(navController) }
        composable(Routes.S_QUOTE) { QuoteScreen(navController) }
        composable(Routes.S_SALE) { ConfirmSaleScreen(navController) }
        composable(Routes.S_SALE_DONE) { SaleDoneScreen(navController) }
        composable(Routes.S_SCANNER) { ScannerScreen(navController) }
        composable(Routes.S_PRODUCT) { ProductDetailScreen(navController, it.arg("id")) }
        composable(Routes.S_STOCK) { StockManagementScreen(navController, it.arg("id")) }
        composable(Routes.S_MOVEMENT) { NewMovementScreen(navController) }
        composable(Routes.S_ALERTS) { AlertsScreen(navController) }
        composable(Routes.S_REPORTS) { ReportsScreen(navController) }
    }
}

private fun NavBackStackEntry.arg(name: String): String = arguments?.getString(name).orEmpty()
