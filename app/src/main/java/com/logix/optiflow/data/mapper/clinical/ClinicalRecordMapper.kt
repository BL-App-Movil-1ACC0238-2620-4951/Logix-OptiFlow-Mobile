package com.logix.optiflow.data.mapper.clinical

import com.logix.optiflow.data.remote.dto.clinical.ClinicalRecordDto
import com.logix.optiflow.data.remote.dto.clinical.OpticalPrescriptionDto
import com.logix.optiflow.domain.model.clinical.ClinicalRecord
import com.logix.optiflow.domain.model.clinical.OpticalPrescription

fun ClinicalRecordDto.toDomain(): ClinicalRecord =
    ClinicalRecord(
        id = id,
        patientId = patientId,
        appointmentId = appointmentId,
        examinationDate = examinationDate,
        observations = observations,
        status = status,
        prescription = prescription?.toDomain(),
    )

private fun OpticalPrescriptionDto.toDomain(): OpticalPrescription =
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
