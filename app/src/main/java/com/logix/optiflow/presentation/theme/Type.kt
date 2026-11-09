package com.logix.optiflow.presentation.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.logix.optiflow.R

private val variableWeights =
    listOf(
        FontWeight.Normal,
        FontWeight.Medium,
        FontWeight.SemiBold,
        FontWeight.Bold,
        FontWeight.ExtraBold,
    )

@OptIn(ExperimentalTextApi::class)
private fun variableFamily(resId: Int): FontFamily =
    FontFamily(
        variableWeights.map { weight ->
            Font(
                resId = resId,
                weight = weight,
                variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
            )
        },
    )

val Poppins =
    FontFamily(
        Font(R.font.poppins_medium, FontWeight.Medium),
        Font(R.font.poppins_semibold, FontWeight.SemiBold),
        Font(R.font.poppins_bold, FontWeight.Bold),
    )

val Jakarta = variableFamily(R.font.plus_jakarta_sans)

val Inter = variableFamily(R.font.inter)

val Geist = variableFamily(R.font.geist)

/** "Open Sans Hebrew Condensed" del diseño: Open Sans variable con ancho 75. */
@OptIn(ExperimentalTextApi::class)
val OpenSansCondensed =
    FontFamily(
        listOf(FontWeight.Normal, FontWeight.Bold).map { weight ->
            Font(
                resId = R.font.open_sans,
                weight = weight,
                variationSettings =
                    FontVariation.Settings(
                        FontVariation.weight(weight.weight),
                        FontVariation.width(75f),
                    ),
            )
        },
    )
