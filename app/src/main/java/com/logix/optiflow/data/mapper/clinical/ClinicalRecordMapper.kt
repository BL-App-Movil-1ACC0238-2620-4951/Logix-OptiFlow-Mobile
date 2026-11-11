package com.logix.optiflow.data.mapper.clinical

import com.logix.optiflow.data.remote.dto.clinical.ClinicalRecordDto
import com.logix.optiflow.data.remote.dto.clinical.MedicalHistoryDto
import com.logix.optiflow.data.remote.dto.clinical.OpticalPrescriptionDto
import com.logix.optiflow.domain.model.clinical.ClinicalRecord
import com.logix.optiflow.domain.model.clinical.MedicalHistory
import com.logix.optiflow.domain.model.clinical.OpticalPrescription

fun ClinicalRecordDto.toDomain(): ClinicalRecord =
    ClinicalRecord(
        id = id,
        patientId = patientId,
        appointmentId = appointmentId,
        examinationDate = examinationDate,
        observations = observations,
        status = status,
        medicalHistory = medicalHistory?.toDomain(),
        prescription = prescription?.toDomain(),
    )

fun MedicalHistoryDto.toDomain(): MedicalHistory =
    MedicalHistory(
        allergies = allergies.orEmpty(),
        previousConditions = previousConditions.orEmpty(),
        familyOcularHistory = familyOcularHistory,
    )

fun OpticalPrescriptionDto.toDomain(): OpticalPrescription =
    OpticalPrescription(
        sphereOD = sphereOD,
        cylinderOD = cylinderOD,
        axisOD = axisOD,
        sphereOS = sphereOS,
        cylinderOS = cylinderOS,
        axisOS = axisOS,
        addition = addition,
        treatment = treatment,
        recommendedFrameType = recommendedFrameType,
    )
