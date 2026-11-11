package com.logix.optiflow.data.demo

/**
 * Modelos de UI del personal (pacientes, agenda y taller). Se llenan con los datos del backend;
 * ya no hay datos de prueba.
 */
data class StaffPatient(
    val id: String,
    val initials: String,
    val name: String,
    val fullName: String,
    val rut: String,
    val note: String,
    val phone: String,
    val lastVisit: String,
    val ficha: String,
    val age: Int,
    val email: String = "",
)

enum class AgendaStatus { WAITING, CONFIRMED, PENDING, PICKUP, DONE }

data class AgendaItem(
    val time: String,
    val duration: String,
    val patientId: String,
    val name: String,
    val service: String,
    val status: AgendaStatus,
    val appointmentId: String? = null,
)

enum class ProductionStage(val label: String) {
    PENDING("Por iniciar"),
    IN_WORKSHOP("En producción"),
    QUALITY_CONTROL("Control calidad"),
    READY_FOR_DELIVERY("Listos"),
    DELIVERED("Entregados"),
}

data class ProductionOrder(
    val code: String,
    val patient: String,
    val detail: String,
    val stage: ProductionStage,
    val due: String,
    val needsAttention: Boolean = false,
    /** Id de la orden en el backend; null en los datos de prueba. */
    val id: String? = null,
)
