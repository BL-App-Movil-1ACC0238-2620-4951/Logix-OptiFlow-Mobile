package com.logix.optiflow.presentation.staff

import com.logix.optiflow.domain.model.searchbooking.StaffRole
import com.logix.optiflow.presentation.shared.components.StaffTab

/** Secciones de la app del personal; cada rol ve solo las que el backend le permite usar. */
enum class StaffFeature {
    AGENDA,
    PATIENTS,
    CLINICAL,
    STOCK,
    INVENTORY,
    QUOTES,
    SALES,
    PRODUCTION,
    ALERTS,
    REPORTS,
}

val StaffRole?.features: Set<StaffFeature>
    get() =
        when (this) {
            StaffRole.OPTOMETRIST -> setOf(StaffFeature.AGENDA, StaffFeature.PATIENTS, StaffFeature.CLINICAL)
            StaffRole.SALES_ADVISOR ->
                setOf(StaffFeature.AGENDA, StaffFeature.PATIENTS, StaffFeature.STOCK, StaffFeature.QUOTES, StaffFeature.SALES)
            StaffRole.TECHNICIAN -> setOf(StaffFeature.PRODUCTION)
            StaffRole.ADMIN -> StaffFeature.entries.toSet()
            null -> emptySet()
        }

fun StaffRole?.can(feature: StaffFeature): Boolean = feature in features

/** Pestañas de la barra inferior: Inicio y Más siempre; el resto según el rol. */
val StaffRole?.tabs: List<StaffTab>
    get() =
        StaffTab.entries.filter { tab ->
            when (tab) {
                StaffTab.HOME, StaffTab.MORE -> true
                StaffTab.AGENDA -> can(StaffFeature.AGENDA)
                StaffTab.PATIENTS -> can(StaffFeature.PATIENTS)
                StaffTab.INVENTORY -> can(StaffFeature.STOCK) || can(StaffFeature.INVENTORY)
            }
        }
