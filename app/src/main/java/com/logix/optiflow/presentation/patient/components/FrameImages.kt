package com.logix.optiflow.presentation.patient.components

import androidx.annotation.DrawableRes
import com.logix.optiflow.R
import com.logix.optiflow.domain.model.inventory.FrameModel
import java.util.Locale

/** El backend no guarda fotos de las monturas; cada SKU usa una de las fotos incluidas en la app. */
private val imagesBySku =
    mapOf(
        "OK-8156-BLK" to R.drawable.img_frame_pacific,
        "PR-17WV-TOR" to R.drawable.img_frame_verona,
        "RB-3025-GLD" to R.drawable.img_frame_havana,
        "RB-5154-BLK" to R.drawable.img_frame_sienna,
        "TF-5401-HAV" to R.drawable.img_frame_amber,
        "VO-5276-TRP" to R.drawable.img_frame_roma,
    )

private val fallbackImages = imagesBySku.values.toList()

@get:DrawableRes
val FrameModel.image: Int
    get() = imagesBySku[sku] ?: fallbackImages[Math.floorMod(sku.hashCode(), fallbackImages.size)]

val FrameModel.priceLabel: String
    get() = String.format(Locale.US, "S/ %.2f", price)

val FrameModel.shapeLabel: String
    get() = shape?.uppercase(Locale.forLanguageTag("es")) ?: "MONTURA"
