package com.logix.optiflow.presentation.patient.components

import android.graphics.BlurMaskFilter
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotateRad
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.logix.optiflow.domain.model.inventory.FrameModel
import com.logix.optiflow.presentation.patient.components.TryOnStyle.AVIATOR
import com.logix.optiflow.presentation.patient.components.TryOnStyle.BROWLINE
import com.logix.optiflow.presentation.patient.components.TryOnStyle.OVAL
import com.logix.optiflow.presentation.patient.components.TryOnStyle.RECTANGULAR
import com.logix.optiflow.presentation.patient.components.TryOnStyle.SQUARE
import kotlin.math.atan2
import kotlin.math.cos
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

/** Acabado del armazón: color base y si es de metal (brillo) o carey (moteado). */
data class FrameFinish(val base: Color, val metal: Boolean, val tortoise: Boolean)

/** Acabado aproximado según el color y el material del catálogo. */
val FrameModel.tryOnFinish: FrameFinish
    get() {
        val name = color.orEmpty().lowercase()
        val metal = material.orEmpty().lowercase().let { "metal" in it && "acetato" !in it }
        return when {
            "carey" in name || "havana" in name -> FrameFinish(Color(0xFF5C3317), metal = false, tortoise = true)
            "dorado" in name -> FrameFinish(Color(0xFFC9A227), metal = true, tortoise = false)
            "plat" in name -> FrameFinish(Color(0xFFB8BEC6), metal = true, tortoise = false)
            "negro" in name -> FrameFinish(Color(0xFF151515), metal = metal, tortoise = false)
            "transparente" in name -> FrameFinish(Color(0xCCDCE3EC), metal = false, tortoise = false)
            else -> FrameFinish(Color(0xFF14254F), metal = metal, tortoise = false)
        }
    }

private val Silver = FrameFinish(Color(0xFFC4C9D0), metal = true, tortoise = false)

/**
 * Dibuja la montura sobre los ojos: se centra entre ambos, escala con su distancia, gira con la
 * inclinación de la cabeza y se angosta al girar la cara hacia un lado ([EyePositions.yaw]).
 */
fun DrawScope.drawGlasses(style: TryOnStyle, finish: FrameFinish, eyes: EyePositions) {
    val (a, b) = if (eyes.left.x <= eyes.right.x) eyes.left to eyes.right else eyes.right to eyes.left
    // Al girar la cara los ojos se acercan en pantalla; se usa el ancho real y luego se comprime.
    val turn = cos(Math.toRadians(eyes.yaw.toDouble().coerceIn(-50.0, 50.0))).toFloat()
    val d = hypot(b.x - a.x, b.y - a.y) / turn
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
    val offsetX = d * 0.55f
    val lensY = center.y + d * 0.05f
    val top = lensY - lensH / 2
    val rim = d * if (finish.metal) 0.035f else 0.075f
    val lenses = listOf(-1f, 1f).map { side -> side to lensPath(style, center.x + side * offsetX, lensY, lensW, lensH, side) }
    val bounds = Rect(center.x - offsetX - lensW / 2, top, center.x + offsetX + lensW / 2, top + lensH)
    val frameBrush = finishBrush(finish, bounds, d)
    val inner = offsetX - lensW / 2
    val bridge = Path().apply {
        moveTo(center.x - inner, top + lensH * 0.3f)
        quadraticTo(center.x, top + lensH * 0.08f, center.x + inner, top + lensH * 0.3f)
    }

    rotateRad(atan2(b.y - a.y, b.x - a.x), center) {
        scale(turn, 1f, center) {
            // Sombra suave del armazón sobre la piel.
            translate(top = d * 0.05f) {
                lenses.forEach { (_, lens) -> blurredStroke(lens, rim * 1.2f, d * 0.035f) }
                blurredStroke(bridge, rim, d * 0.035f)
            }
            // Patillas: salen de la bisagra y se pierden hacia las orejas.
            for (side in listOf(-1f, 1f)) {
                val hinge = Offset(center.x + side * (offsetX + lensW / 2), top + lensH * 0.2f)
                val end = Offset(hinge.x + side * d * 0.3f, hinge.y - lensH * 0.04f)
                drawLine(
                    Brush.linearGradient(listOf(finish.base, finish.base.copy(alpha = 0f)), hinge, end),
                    hinge,
                    end,
                    rim * 0.9f,
                    StrokeCap.Round,
                )
            }
            lenses.forEach { (_, lens) -> drawLens(lens, lensW, sun = style == AVIATOR) }
            when (style) {
                BROWLINE -> {
                    // Aro inferior de metal fino y ceja gruesa de acetato arriba.
                    val metalBrush = finishBrush(Silver, bounds, d)
                    lenses.forEach { (_, lens) -> drawPath(lens, metalBrush, style = Stroke(width = d * 0.025f)) }
                    clipRect(bounds.left - d, top - d, bounds.right + d, top + lensH * 0.4f) {
                        lenses.forEach { (_, lens) -> drawPath(lens, frameBrush, style = Stroke(width = d * 0.13f)) }
                    }
                }
                else -> lenses.forEach { (_, lens) -> drawPath(lens, frameBrush, style = Stroke(width = rim)) }
            }
            // Borde de luz en la parte superior del aro.
            clipRect(bounds.left - d, top - d, bounds.right + d, top + lensH * 0.3f) {
                lenses.forEach { (_, lens) -> drawPath(lens, Color.White.copy(alpha = 0.28f), style = Stroke(width = rim * 0.22f)) }
            }
            drawPath(bridge, if (style == BROWLINE) finishBrush(Silver, bounds, d) else frameBrush, style = Stroke(width = rim, cap = StrokeCap.Round))
            if (style == AVIATOR) {
                val y = top + rim / 2
                drawLine(frameBrush, Offset(center.x - inner, y), Offset(center.x + inner, y), rim * 0.8f, StrokeCap.Round)
            }
            if (finish.metal || style == BROWLINE) {
                // Plaquetas transparentes para la nariz.
                for (side in listOf(-1f, 1f)) {
                    val pad = Rect(Offset(center.x + side * inner * 0.85f, top + lensH * 0.72f), d * 0.025f)
                    drawOval(Color.White.copy(alpha = 0.12f), pad.topLeft, pad.size)
                    drawOval(finish.base.copy(alpha = 0.5f), pad.topLeft, pad.size, style = Stroke(width = d * 0.006f))
                }
            } else {
                // Remaches metálicos en las bisagras, típicos del acetato.
                for (side in listOf(-1f, 1f)) {
                    val stud = Offset(center.x + side * (offsetX + lensW * 0.4f), top + lensH * 0.16f)
                    drawCircle(Brush.radialGradient(listOf(Color.White, Color(0xFF9AA1AB)), stud, rim * 0.4f), rim * 0.32f, stud)
                }
            }
        }
    }
}

/** Lente con dos reflejos diagonales: casi transparente, o en degradado marrón si es de sol ([sun]). */
private fun DrawScope.drawLens(lens: Path, w: Float, sun: Boolean) {
    val box = lens.getBounds()
    val tint = if (sun) listOf(Color(0xB04A2C17), Color(0x407A4E2E)) else listOf(Color(0x2690A4C0), Color(0x0890A4C0))
    drawPath(lens, Brush.verticalGradient(tint, box.top, box.bottom))
    clipPath(lens) {
        drawLine(Color.White.copy(alpha = 0.3f), Offset(box.left + w * 0.15f, box.bottom), Offset(box.left + w * 0.55f, box.top), w * 0.1f)
        drawLine(Color.White.copy(alpha = 0.18f), Offset(box.left + w * 0.35f, box.bottom), Offset(box.left + w * 0.72f, box.top), w * 0.04f)
    }
}

/** Degradado del armazón: brillo de metal, moteado de carey o relieve de acetato. */
private fun finishBrush(finish: FrameFinish, bounds: Rect, d: Float): Brush {
    val base = finish.base
    return when {
        finish.tortoise -> Brush.linearGradient(
            listOf(Color(0xFF2E170A), Color(0xFF8A4B1C), Color(0xFF3B1F0E), Color(0xFFB8702F), Color(0xFF4A2610)),
            start = bounds.topLeft,
            end = bounds.topLeft + Offset(d * 0.22f, d * 0.12f),
            tileMode = TileMode.Mirror,
        )
        finish.metal -> Brush.verticalGradient(
            listOf(lerp(base, Color.White, 0.6f), base, lerp(base, Color.Black, 0.35f), lerp(base, Color.White, 0.3f), lerp(base, Color.Black, 0.2f)),
            bounds.top,
            bounds.bottom,
        )
        else -> Brush.verticalGradient(
            listOf(lerp(base, Color.White, 0.22f), base, lerp(base, Color.Black, 0.3f)),
            bounds.top,
            bounds.bottom,
        )
    }
}

/** Trazo negro difuminado para la sombra. */
private fun DrawScope.blurredStroke(path: Path, width: Float, blur: Float) {
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = width
        color = Color.Black.copy(alpha = 0.3f).toArgb()
        maskFilter = BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL)
    }
    drawIntoCanvas { it.nativeCanvas.drawPath(path.asAndroidPath(), paint) }
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
                moveTo(x(-0.46f), topY + h * 0.06f)
                quadraticTo(x(0.05f), topY - h * 0.04f, x(0.5f), topY + h * 0.02f)
                cubicTo(x(0.56f), topY + h * 0.45f, x(0.42f), topY + h * 0.95f, x(0.08f), topY + h)
                cubicTo(x(-0.25f), topY + h * 1.02f, x(-0.5f), topY + h * 0.6f, x(-0.46f), topY + h * 0.06f)
                close()
            }
            else -> {
                val radius = h * if (style == SQUARE) 0.2f else 0.18f
                addRoundRect(RoundRect(left, topY, left + w, topY + h, CornerRadius(radius)))
            }
        }
    }
}
