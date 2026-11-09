package com.logix.optiflow.domain.model.clinical

import java.time.Instant
import java.util.UUID

data class ClinicalRecord(
    val id: UUID,
    val patientId: UUID?,
    val appointmentId: UUID?,
    val examinationDate: Instant?,
    val observations: String?,
    val status: String?,
    val prescription: OpticalPrescription?,
)

data class OpticalPrescription(
    val sphereOD: Double?,
    val cylinderOD: Double?,
    val axisOD: Int?,
    val sphereOS: Double?,
    val cylinderOS: Double?,
    val axisOS: Int?,
    val addition: Double?,
    val treatment: String?,
    val recommendedFrameType: String?,
)
