package com.logix.optiflow.data.mapper.production

import com.logix.optiflow.data.remote.dto.production.LensDto
import com.logix.optiflow.data.remote.dto.production.StatusChangeDto
import com.logix.optiflow.data.remote.dto.production.WorkOrderDto
import com.logix.optiflow.domain.model.production.DeliveryDelay
import com.logix.optiflow.domain.model.production.Lens
import com.logix.optiflow.domain.model.production.NamedRef
import com.logix.optiflow.domain.model.production.StatusChange
import com.logix.optiflow.domain.model.production.WorkOrder

fun WorkOrderDto.toDomain(): WorkOrder =
    WorkOrder(
        id = id,
        saleId = saleId,
        patientId = patientId,
        opticalStoreId = opticalStoreId,
        status = status,
        estimatedDeliveryDate = estimatedDeliveryDate,
        delayed = delayed,
        technician = technician?.let { NamedRef(it.id, it.name) },
        laboratory = laboratory?.let { NamedRef(it.id, it.name) },
        lenses = lenses.orEmpty().map { it.toDomain() },
        deliveryDelay = deliveryDelay?.let { DeliveryDelay(it.reason, it.newEstimatedDeliveryDate) },
        statusHistory = statusHistory.orEmpty().map { it.toDomain() },
        updatedAt = updatedAt,
        createdAt = createdAt,
        deliveredAt = deliveredAt,
    )

private fun LensDto.toDomain(): Lens = Lens(id = id, specifications = specifications, completed = completed)

private fun StatusChangeDto.toDomain(): StatusChange = StatusChange(status = status, changedAt = changedAt)
