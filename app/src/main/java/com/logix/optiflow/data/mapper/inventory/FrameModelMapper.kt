package com.logix.optiflow.data.mapper.inventory

import com.logix.optiflow.data.remote.dto.inventory.FrameModelDto
import com.logix.optiflow.domain.model.inventory.FrameModel

fun FrameModelDto.toDomain(): FrameModel =
    FrameModel(
        id = id,
        sku = sku,
        name = name,
        material = material,
        shape = shape,
        color = color,
        price = price,
        currency = currency ?: "PEN",
        tryOnModelUrl = tryOnModelUrl,
        quantity = quantity,
        minimumStock = minimumStock,
    )
