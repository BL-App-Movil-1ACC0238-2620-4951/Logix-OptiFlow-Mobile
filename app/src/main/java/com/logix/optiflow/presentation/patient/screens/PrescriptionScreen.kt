package com.logix.optiflow.presentation.patient.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.presentation.navigation.Routes
import com.logix.optiflow.presentation.patient.components.PatientScreen
import com.logix.optiflow.presentation.patient.components.patientViewModel
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.PatientDarkTopBar
import com.logix.optiflow.presentation.shared.components.SolidButton
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.card
import com.logix.optiflow.presentation.theme.DeepNavy
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.InfoBanner
import com.logix.optiflow.presentation.theme.Jakarta
import com.logix.optiflow.presentation.theme.NavyBlue
import com.logix.optiflow.presentation.theme.SkyTint
import com.logix.optiflow.presentation.theme.Slate300
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.Slate500
import com.logix.optiflow.presentation.theme.White

private val Ink = Color(0xFF111827)
private val TableBorder = Color(0xFFCBD5E1)
private val Soft = Color(0xFFF4F6FD)
private val DpBox = Color(0xFFE7EEFC)
private val ChipBg = Color(0xFFDCE6FB)
private val Teal = Color(0xFF0F766E)

@Composable
fun PrescriptionScreen(nav: NavController, editable: Boolean) {
    val vm = patientViewModel()
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    val rx = state.prescription

    val values =
        remember(editable) {
            mutableStateListOf(
                *if (editable) Array(8) { "" } else arrayOf(
                    rx.sphereOD, rx.cylinderOD, rx.axisOD, rx.additionOD,
                    rx.sphereOI, rx.cylinderOI, rx.axisOI, rx.additionOI,
                ),
            )
        }
    var pd by remember(editable) { mutableStateOf(if (editable) "" else rx.pupillaryDistance) }
    var notes by remember(editable) { mutableStateOf(if (editable) "" else rx.observations) }
    val measures = remember(editable) { mutableStateListOf(*if (editable) Array(4) { "" } else arrayOf("52", "18", "140", "32")) }

    PatientScreen(
        nav = nav,
        tab = null,
        topBar = { PatientDarkTopBar("Receta óptica", onBack = { nav.popBackStack() }) },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            VSpace(12.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(InfoBanner)
                    .border(1.dp, SkyTint, RoundedCornerShape(16.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(56.dp).clip(CircleShape).background(White), contentAlignment = Alignment.Center) {
                    LIcon(R.drawable.lucide_ic_badge_check, Teal, 30.dp)
                }
                HSpace(16.dp)
                Column {
                    Txt("Tu receta está vigente", 17, weight = FontWeight.Bold, color = DeepNavy)
                    Txt("${rx.issuedLabel} · vence en 5 meses", 13, color = Slate500)
                }
            }
            VSpace(14.dp)
            Column(
                Modifier
                    .fillMaxWidth()
                    .card(radius = 24.dp, elevation = 2.dp)
                    .padding(16.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Soft).padding(12.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Txt("Paciente", 11, color = Slate400)
                        Txt(rx.patientName, 13, weight = FontWeight.Bold, color = Ink)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Txt("Fecha", 11, color = Slate400)
                        Txt(rx.date, 14, weight = FontWeight.Bold, color = Ink)
                    }
                }
                VSpace(14.dp)
                PrescriptionTable(values, editable)
                VSpace(14.dp)
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DpBox).padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LIcon(R.drawable.lucide_ic_eye, DeepNavy, 18.dp)
                    HSpace(10.dp)
                    Txt("Distancia pupilar (DP)", 13, color = DeepNavy, modifier = Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (editable) {
                                CellInput(pd, { pd = it }, "0", 16, Modifier.width(40.dp), TextAlign.End, FontWeight.Bold)
                            } else {
                                Txt(pd, 16, weight = FontWeight.Bold, color = DeepNavy)
                            }
                            Txt(" mm", 16, weight = FontWeight.Bold, color = DeepNavy)
                        }
                        Txt("31 / 31 mm", 10, color = Slate500)
                    }
                }
                VSpace(16.dp)
                SectionCaption("DIAGNÓSTICOS ASOCIADOS")
                VSpace(10.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rx.diagnoses.forEach { DiagnosisChip(it) }
                    if (editable) {
                        Box(
                            Modifier.height(30.dp).clip(CircleShape).background(ChipBg).padding(horizontal = 22.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            LIcon(R.drawable.lucide_ic_plus, Electric, 16.dp)
                        }
                    }
                }
                VSpace(14.dp)
                ThinDivider()
                VSpace(10.dp)
                SectionCaption("MEDIDAS DE LA MONTURA")
                VSpace(12.dp)
                Row(Modifier.fillMaxWidth()) {
                    listOf("Calibre", "Puente", "Patillas", "Alto").forEachIndexed { index, label ->
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            MeasureGlyph(index)
                            VSpace(6.dp)
                            if (editable) {
                                CellInput(measures[index], { measures[index] = it }, "0", 13, Modifier.width(44.dp), TextAlign.Center, FontWeight.Bold)
                            } else {
                                Txt(measures[index], 13, weight = FontWeight.Bold, color = DeepNavy)
                            }
                            Txt(label, 10, color = Slate400)
                        }
                    }
                }
                VSpace(12.dp)
                ThinDivider()
                VSpace(10.dp)
                SectionCaption("OBSERVACIONES E INDICACIÓN")
                VSpace(8.dp)
                if (editable) {
                    CellInput(notes, { notes = it }, "Escriba observación", 13, Modifier.fillMaxWidth(), TextAlign.Start, FontWeight.Normal, singleLine = false)
                } else {
                    Txt(notes, 13, color = Color(0xFF374151), lineHeight = 19.sp)
                }
                VSpace(14.dp)
                Canvas(Modifier.fillMaxWidth().height(1.dp)) {
                    drawLine(
                        Slate300,
                        Offset(0f, 0f),
                        Offset(size.width, 0f),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
                    )
                }
                VSpace(12.dp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        Txt("Dra. Fernanda Soto", 12, weight = FontWeight.Bold, color = Ink)
                        Txt("Oftalmología · Reg. 18452", 10, color = Slate400)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.width(80.dp).height(1.dp).background(Ink))
                        VSpace(4.dp)
                        Txt("Firma profesional", 9, color = Slate400)
                    }
                }
            }
            VSpace(16.dp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                LIcon(R.drawable.lucide_ic_shield_check, Slate500, 16.dp)
                HSpace(8.dp)
                Txt("Tus datos de salud se almacenan de forma segura.", 12, color = Slate500)
            }
            VSpace(16.dp)
            if (editable) {
                SolidButton(
                    "Guardar",
                    onClick = {
                        vm.savePrescription(
                            rx.copy(
                                sphereOD = values[0].ifBlank { "0.00" },
                                cylinderOD = values[1].ifBlank { "0.00" },
                                axisOD = values[2].ifBlank { "0" }.let { if (it.endsWith("°")) it else "$it°" },
                                additionOD = values[3].ifBlank { "0.00" },
                                sphereOI = values[4].ifBlank { "0.00" },
                                cylinderOI = values[5].ifBlank { "0.00" },
                                axisOI = values[6].ifBlank { "0" }.let { if (it.endsWith("°")) it else "$it°" },
                                additionOI = values[7].ifBlank { "0.00" },
                                pupillaryDistance = pd.ifBlank { "0" },
                                observations = notes,
                            ),
                        )
                        Toast.makeText(context, "Receta actualizada", Toast.LENGTH_SHORT).show()
                        nav.popBackStack()
                    },
                    height = 48.dp,
                    radius = 12.dp,
                    fontSize = 16,
                )
            } else {
                SolidButton(
                    "Actualizar receta",
                    onClick = { nav.navigate(Routes.P_PRESCRIPTION_EDIT) },
                    background = White,
                    contentColor = Electric,
                    border = SkyTint,
                    height = 48.dp,
                    radius = 12.dp,
                    fontSize = 16,
                    leadingIcon = R.drawable.lucide_ic_refresh_cw,
                )
            }
            VSpace(16.dp)
        }
    }
}

@Composable
private fun PrescriptionTable(values: MutableList<String>, editable: Boolean) {
    val shape = RoundedCornerShape(12.dp)
    Column(Modifier.fillMaxWidth().clip(shape).border(1.dp, TableBorder, shape)) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            HeaderCell("Ojo", null, 1.3f)
            HeaderCell("Esfera", "SPH", 1f)
            HeaderCell("Cilindro", "CYL", 1f)
            HeaderCell("Eje", "AXIS", 0.8f)
            HeaderCell("Prisma", null, 1f)
            HeaderCell("Adición", "ADD", 1f, last = true)
        }
        listOf("R (OD)" to "Derecho", "L (OI)" to "Izquierdo").forEachIndexed { row, (eye, side) ->
            Box(Modifier.fillMaxWidth().height(1.dp).background(TableBorder))
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                Cell(1.3f) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Txt(eye, 13, weight = FontWeight.Bold, color = Ink)
                        Txt(side, 9, color = Slate400)
                    }
                }
                listOf(0 to 1f, 1 to 1f, 2 to 0.8f).forEach { (col, w) ->
                    Cell(w) { ValueCell(values, row * 4 + col, editable, if (col == 2) "0°" else "0.00") }
                }
                Cell(1f) { Txt("—", 13, color = Slate400) }
                Cell(1f, last = true) { ValueCell(values, row * 4 + 3, editable, "0.00") }
            }
        }
    }
}

@Composable
private fun ValueCell(values: MutableList<String>, index: Int, editable: Boolean, placeholder: String) {
    if (editable) {
        CellInput(values[index], { values[index] = it }, placeholder, 13, Modifier.fillMaxWidth(), TextAlign.Center, FontWeight.Medium)
    } else {
        Txt(values[index], 13, weight = FontWeight.Medium, color = Ink)
    }
}

@Composable
private fun RowScope.HeaderCell(title: String, caption: String?, weight: Float, last: Boolean = false) {
    Cell(weight, last, vertical = 10.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Txt(title, 10, weight = FontWeight.Bold, color = Ink)
            if (caption != null) Txt(caption, 8, color = Slate400)
        }
    }
}

@Composable
private fun RowScope.Cell(
    weight: Float,
    last: Boolean = false,
    vertical: androidx.compose.ui.unit.Dp = 14.dp,
    content: @Composable () -> Unit,
) {
    Row(Modifier.weight(weight).fillMaxHeight()) {
        Box(
            Modifier.weight(1f).fillMaxHeight().padding(vertical = vertical, horizontal = 2.dp),
            contentAlignment = Alignment.Center,
        ) { content() }
        if (!last) Box(Modifier.width(1.dp).fillMaxHeight().background(TableBorder))
    }
}

@Composable
private fun CellInput(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    size: Int,
    modifier: Modifier,
    align: TextAlign,
    weight: FontWeight,
    singleLine: Boolean = true,
) {
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = singleLine,
        modifier = modifier,
        cursorBrush = SolidColor(Electric),
        keyboardOptions = KeyboardOptions(keyboardType = if (singleLine) KeyboardType.Text else KeyboardType.Text),
        textStyle = TextStyle(fontFamily = Jakarta, fontSize = size.sp, fontWeight = weight, color = Ink, textAlign = align),
        decorationBox = { inner ->
            Box(contentAlignment = if (align == TextAlign.Start) Alignment.CenterStart else Alignment.Center) {
                if (value.isEmpty()) Txt(placeholder, size, weight = weight, color = Slate400, align = align)
                inner()
            }
        },
    )
}

@Composable
private fun SectionCaption(text: String) {
    Txt(text, 10, weight = FontWeight.Bold, color = DeepNavy, letterSpacing = 0.6.sp)
}

@Composable
private fun DiagnosisChip(text: String) {
    Box(
        Modifier.height(30.dp).clip(CircleShape).background(ChipBg).padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Txt(text, 13, weight = FontWeight.Medium, color = Electric)
    }
}

@Composable
private fun ThinDivider() = Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEEF2F7)))

/** Iconos de medidas de montura (calibre, puente, patillas, alto). */
@Composable
private fun MeasureGlyph(kind: Int) {
    Canvas(Modifier.size(width = 30.dp, height = 18.dp)) {
        val stroke = Stroke(width = 2.dp.toPx())
        when (kind) {
            0 -> drawRoundRect(NavyBlue, Offset(1.dp.toPx(), 2.dp.toPx()), Size(size.width - 2.dp.toPx(), size.height - 4.dp.toPx()), CornerRadius(8.dp.toPx()), style = stroke)
            1 -> drawArc(NavyBlue, 200f, 140f, false, Offset(3.dp.toPx(), 4.dp.toPx()), Size(size.width - 6.dp.toPx(), size.height * 1.4f), style = stroke)
            2 -> drawLine(NavyBlue, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 2.dp.toPx())
            else -> {
                drawLine(NavyBlue, Offset(size.width / 2 - 3.dp.toPx(), 1.dp.toPx()), Offset(size.width / 2 - 3.dp.toPx(), size.height - 1.dp.toPx()), 2.dp.toPx())
                drawLine(NavyBlue, Offset(size.width / 2 + 3.dp.toPx(), 1.dp.toPx()), Offset(size.width / 2 + 3.dp.toPx(), size.height - 1.dp.toPx()), 2.dp.toPx())
            }
        }
    }
}

