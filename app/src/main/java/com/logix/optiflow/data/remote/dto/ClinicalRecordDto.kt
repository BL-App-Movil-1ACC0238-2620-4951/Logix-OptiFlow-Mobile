package com.logix.optiflow.data.remote.dto

import java.time.Instant
import java.util.UUID

data class ClinicalRecordsSummaryDto(
    val patientsWithRecords: Long,
)

data class ClinicalRecordDto(
    val id: UUID,
    val patientId: UUID?,
    val appointmentId: UUID?,
    val examinationDate: Instant?,
    val observations: String?,
    val status: String?,
    val prescription: OpticalPrescriptionDto?,
)

data class OpticalPrescriptionDto(
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
