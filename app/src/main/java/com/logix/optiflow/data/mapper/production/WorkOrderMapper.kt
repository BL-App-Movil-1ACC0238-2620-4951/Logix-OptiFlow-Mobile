package com.logix.optiflow.data.mapper.production

import com.logix.optiflow.data.remote.dto.production.LensDto
import com.logix.optiflow.data.remote.dto.production.StatusChangeDto
import com.logix.optiflow.data.remote.dto.production.WorkOrderDto
import com.logix.optiflow.domain.model.production.Lens
import com.logix.optiflow.domain.model.production.StatusChange
import com.logix.optiflow.domain.model.production.WorkOrder

fun WorkOrderDto.toDomain(): WorkOrder =
    WorkOrder(
        id = id,
        patientId = patientId,
        opticalStoreId = opticalStoreId,
        status = status,
        estimatedDeliveryDate = estimatedDeliveryDate,
        delayed = delayed,
        lenses = lenses.orEmpty().map { it.toDomain() },
        statusHistory = statusHistory.orEmpty().map { it.toDomain() },
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

private fun LensDto.toDomain(): Lens = Lens(id = id, specifications = specifications, completed = completed)

private fun StatusChangeDto.toDomain(): StatusChange = StatusChange(status = status, changedAt = changedAt)
