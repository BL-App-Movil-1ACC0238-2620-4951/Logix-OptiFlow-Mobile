package com.logix.optiflow.presentation.patient.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotateRad
import com.logix.optiflow.domain.model.inventory.FrameModel
import com.logix.optiflow.presentation.patient.components.TryOnStyle.AVIATOR
import com.logix.optiflow.presentation.patient.components.TryOnStyle.BROWLINE
import com.logix.optiflow.presentation.patient.components.TryOnStyle.OVAL
import com.logix.optiflow.presentation.patient.components.TryOnStyle.RECTANGULAR
import com.logix.optiflow.presentation.patient.components.TryOnStyle.SQUARE
import kotlin.math.atan2
import kotlin.math.hypot

/** Modelos de prueba virtual que la app sabe dibujar; el backend los indica en `tryOnModelUrl` (p. ej. `try-on/aviator`). */
enum class TryOnStyle(val key: String) {
    RECTANGULAR("rectangular"),
    OVAL("oval"),
    AVIATOR("aviator"),
    BROWLINE("browline"),
    SQUARE("square"),
}

/** Modelo de prueba de la montura, o null si no tiene (entonces no se ofrece "Probar"). */
val FrameModel.tryOnStyle: TryOnStyle?
    get() = tryOnModelUrl?.let { url ->
        val key = url.substringAfterLast('/').substringBefore('.').lowercase()
        TryOnStyle.entries.firstOrNull { it.key == key } ?: RECTANGULAR
    }

/** Color aproximado del armazón según el nombre de color del catálogo. */
val FrameModel.tryOnColor: Color
    get() {
        val name = color.orEmpty().lowercase()
        return when {
            "negro" in name -> Color(0xFF111111)
            "carey" in name || "havana" in name -> Color(0xFF5C3317)
            "dorado" in name -> Color(0xFFC9A227)
            "plat" in name -> Color(0xFFB8BEC6)
            "transparente" in name -> Color(0xCCDCE3EC)
            else -> Color(0xFF14254F)
        }
    }

/**
 * Dibuja la montura sobre los ojos: se centra entre ambos, escala con su distancia y gira con la
 * inclinación de la cabeza.
 */
fun DrawScope.drawGlasses(style: TryOnStyle, color: Color, eyes: EyePositions) {
    val (a, b) = if (eyes.left.x <= eyes.right.x) eyes.left to eyes.right else eyes.right to eyes.left
    val d = hypot(b.x - a.x, b.y - a.y)
    if (d < 1f) return
    val center = Offset((a.x + b.x) / 2, (a.y + b.y) / 2)
    val lensW = d * 0.8f
    val lensH = lensW * when (style) {
        RECTANGULAR -> 0.5f
        OVAL -> 0.7f
        AVIATOR -> 0.85f
        BROWLINE -> 0.6f
        SQUARE -> 0.8f
    }
    val rim = d * 0.07f
    val offsetX = d * 0.55f
    val lensY = center.y + d * 0.05f
    val top = lensY - lensH / 2

    rotateRad(atan2(b.y - a.y, b.x - a.x), center) {
        for (side in listOf(-1f, 1f)) {
            val cx = center.x + side * offsetX
            val lens = lensPath(style, cx, lensY, lensW, lensH, side)
            drawPath(lens, color.copy(alpha = 0.12f), style = Fill)
            drawPath(lens, color, style = Stroke(width = if (style == BROWLINE) rim * 0.5f else rim))
            if (style == BROWLINE) {
                drawLine(color, Offset(cx - lensW / 2, top + rim * 0.6f), Offset(cx + lensW / 2, top + rim * 0.6f), rim * 1.8f, StrokeCap.Round)
            }
            // Patilla hacia la oreja.
            val outer = cx + side * lensW / 2
            drawLine(color, Offset(outer, top + lensH * 0.2f), Offset(outer + side * d * 0.25f, top + lensH * 0.15f), rim, StrokeCap.Round)
        }
        val inner = offsetX - lensW / 2
        val bridgeY = top + lensH * 0.3f
        val bridge = Path().apply {
            moveTo(center.x - inner, bridgeY)
            quadraticTo(center.x, bridgeY - lensH * 0.2f, center.x + inner, bridgeY)
        }
        drawPath(bridge, color, style = Stroke(width = rim, cap = StrokeCap.Round))
        if (style == AVIATOR) {
            drawLine(color, Offset(center.x - inner, top + rim / 2), Offset(center.x + inner, top + rim / 2), rim * 0.7f, StrokeCap.Round)
        }
    }
}

/** Contorno de un lente centrado en ([cx], [cy]); [side] es -1 para el izquierdo y 1 para el derecho. */
private fun lensPath(style: TryOnStyle, cx: Float, cy: Float, w: Float, h: Float, side: Float): Path {
    val left = cx - w / 2
    val topY = cy - h / 2
    return Path().apply {
        when (style) {
            OVAL -> addOval(Rect(left, topY, left + w, topY + h))
            AVIATOR -> {
                // Gota: arriba casi recta y la parte baja más ancha hacia afuera.
                fun x(t: Float) = cx + side * t * w
                moveTo(x(-0.47f), topY + h * 0.08f)
                lineTo(x(0.5f), topY)
                cubicTo(x(0.56f), topY + h * 0.55f, x(0.3f), topY + h, x(0.02f), topY + h)
                cubicTo(x(-0.35f), topY + h, x(-0.52f), topY + h * 0.5f, x(-0.47f), topY + h * 0.08f)
                close()
            }
            else -> {
                val radius = h * if (style == SQUARE) 0.2f else 0.18f
                addRoundRect(RoundRect(left, topY, left + w, topY + h, CornerRadius(radius)))
            }
        }
    }
}
