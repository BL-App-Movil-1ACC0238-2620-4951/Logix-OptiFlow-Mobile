package com.logix.optiflow.presentation.navigation

import androidx.navigation.NavController
import com.logix.optiflow.presentation.shared.components.PatientTab
import com.logix.optiflow.presentation.shared.components.StaffTab
import java.util.UUID

object Routes {
    const val AUTH = "auth"

    // Paciente
    const val P_HOME = "p/home"
    const val P_NOTIFICATIONS = "p/notifications"
    const val P_SEARCH = "p/search"
    const val P_CATALOG = "p/catalog"
    const val P_FRAME = "p/frame/{id}"
    const val P_TRYON = "p/tryon/{id}"
    const val P_PRESCRIPTION = "p/prescription"
    const val P_PRESCRIPTION_EDIT = "p/prescription/edit"
    const val P_APPOINTMENTS = "p/appointments"
    const val P_APPOINTMENT = "p/appointment/{id}"
    const val P_RESCHEDULE = "p/reschedule/{id}"
    const val P_BOOK = "p/book?storeId={storeId}"
    const val P_ORDERS = "p/orders"
    const val P_ORDER = "p/order/{id}"
    const val P_PROFILE = "p/profile"
    const val P_HISTORY = "p/history"
    const val P_SETTINGS = "p/settings"

    fun frame(id: String) = "p/frame/$id"
    fun tryOn(id: String) = "p/tryon/$id"
    fun appointment(id: String) = "p/appointment/$id"
    fun reschedule(id: String) = "p/reschedule/$id"
    fun book(storeId: UUID? = null) = if (storeId == null) "p/book" else "p/book?storeId=$storeId"
    fun order(id: String) = "p/order/$id"

    // Personal clínico
    const val S_HOME = "s/home"
    const val S_AGENDA = "s/agenda"
    const val S_PATIENTS = "s/patients"
    const val S_INVENTORY = "s/inventory"
    const val S_MORE = "s/more"
    const val S_NEW_PATIENT = "s/patients/new"
    const val S_PATIENT = "s/patient/{id}"
    const val S_NEW_APPOINTMENT = "s/appointments/new"
    const val S_CLINICAL = "s/clinical"
    const val S_EVALUATION = "s/evaluation"
    const val S_WORK_ORDER = "s/work-order"
    const val S_PRODUCTION = "s/production"
    const val S_QUOTE = "s/quote"
    const val S_SALE = "s/sale"
    const val S_SALE_DONE = "s/sale/done"
    const val S_SCANNER = "s/scanner"
    const val S_PRODUCT = "s/product/{id}"
    const val S_STOCK = "s/stock/{id}"
    const val S_MOVEMENT = "s/movement"
    const val S_ALERTS = "s/alerts"
    const val S_REPORTS = "s/reports"

    fun staffPatient(id: String) = "s/patient/$id"
    fun product(id: String) = "s/product/$id"
    fun stock(id: String) = "s/stock/$id"
}

fun NavController.goPatientTab(tab: PatientTab) {
    val route =
        when (tab) {
            PatientTab.HOME -> Routes.P_HOME
            PatientTab.SEARCH -> Routes.P_SEARCH
            PatientTab.APPOINTMENTS -> Routes.P_APPOINTMENTS
            PatientTab.ORDERS -> Routes.P_ORDERS
            PatientTab.PROFILE -> Routes.P_PROFILE
        }
    navigate(route) {
        popUpTo(Routes.P_HOME)
        launchSingleTop = true
    }
}

fun NavController.goStaffTab(tab: StaffTab) {
    val route =
        when (tab) {
            StaffTab.HOME -> Routes.S_HOME
            StaffTab.AGENDA -> Routes.S_AGENDA
            StaffTab.PATIENTS -> Routes.S_PATIENTS
            StaffTab.INVENTORY -> Routes.S_INVENTORY
            StaffTab.MORE -> Routes.S_MORE
        }
    navigate(route) {
        popUpTo(Routes.S_HOME)
        launchSingleTop = true
    }
}

/** Cambia de experiencia (paciente ↔ personal) limpiando la pila. */
fun NavController.switchRoot(route: String) {
    navigate(route) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}
