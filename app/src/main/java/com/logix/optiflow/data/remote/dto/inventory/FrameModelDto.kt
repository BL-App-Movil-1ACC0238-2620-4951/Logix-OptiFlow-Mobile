package com.logix.optiflow.data.remote.dto.inventory

import java.util.UUID

data class FrameModelDto(
    val id: UUID,
    val sku: String,
    val name: String,
    val material: String?,
    val shape: String?,
    val color: String?,
    val price: Double,
    val currency: String?,
    val tryOnModelUrl: String?,
    val quantity: Int?,
    val minimumStock: Int?,
)

data class AddFrameModelRequest(
    val sku: String,
    val name: String,
    val material: String?,
    val shape: String?,
    val color: String?,
    val price: Double,
    val tryOnModelUrl: String? = null,
)

data class UpdateFramePriceRequest(val price: Double)
