package com.logix.optiflow.presentation.patient.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.presentation.navigation.Routes
import com.logix.optiflow.presentation.patient.components.PatientScreen
import com.logix.optiflow.presentation.patient.components.patientViewModel
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.IconTile
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.PatientDarkTopBar
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.card
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.theme.DeepNavy
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.InfoBanner
import com.logix.optiflow.presentation.theme.Lavender
import com.logix.optiflow.presentation.theme.NavyBlue
import com.logix.optiflow.presentation.theme.SkyTint
import com.logix.optiflow.presentation.theme.Slate300
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.Slate500
import com.logix.optiflow.presentation.theme.White

private val Ink = Color(0xFF111827)
private val TableBorder = Color(0xFFCBD5E1)
private val Soft = Color(0xFFF4F6FD)
private val ChipBg = Color(0xFFDCE6FB)
private val Teal = Color(0xFF0F766E)

@Composable
private fun NoPrescription(nav: NavController) {
    PatientScreen(
        nav = nav,
        tab = null,
        topBar = { PatientDarkTopBar("Receta óptica", onBack = { nav.popBackStack() }) },
    ) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Txt("Aún no tienes una receta registrada.", 16, weight = FontWeight.Bold, color = NavyBlue)
            VSpace(8.dp)
            Txt("Aparecerá aquí después de tu evaluación visual en la óptica.", 13, color = Slate500)
        }
    }
}

@Composable
fun PrescriptionListScreen(nav: NavController) {
    val vm = patientViewModel()
    val state by vm.uiState.collectAsState()
    if (!state.isLoading && state.prescriptions.isEmpty()) {
        NoPrescription(nav)
        return
    }
    PatientScreen(
        nav = nav,
        tab = null,
        topBar = { PatientDarkTopBar("Mis recetas", onBack = { nav.popBackStack() }) },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            VSpace(24.dp)
            if (state.isLoading) Txt("Cargando tus recetas...", 14, color = Slate500)
            state.prescriptions.forEachIndexed { index, item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .card(radius = 16.dp, border = Lavender, elevation = 2.dp)
                        .tap { nav.navigate(Routes.prescription(item.recordId)) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconTile(R.drawable.lucide_ic_file_text, Lavender, Electric, size = 40.dp, iconSize = 18.dp, radius = 12.dp)
                    HSpace(12.dp)
                    Column(Modifier.weight(1f)) {
                        Txt(if (index == 0) "Receta vigente" else "Receta anterior", 14, weight = FontWeight.Bold, color = DeepNavy)
                        Txt("${item.date} · ${item.summary}", 12, color = Slate500)
                    }
                    LIcon(R.drawable.lucide_ic_chevron_right, Slate400, 18.dp)
                }
                VSpace(12.dp)
            }
        }
    }
}

@Composable
private fun LoadingPrescription(nav: NavController, error: String?, onRetry: () -> Unit) {
    PatientScreen(
        nav = nav,
        tab = null,
        topBar = { PatientDarkTopBar("Receta óptica", onBack = { nav.popBackStack() }) },
    ) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Txt(error ?: "Cargando receta...", 14, color = Slate500)
            if (error != null) {
                VSpace(10.dp)
                Txt("Reintentar", 14, weight = FontWeight.Bold, color = Electric, modifier = Modifier.tap(onRetry))
            }
        }
    }
}

@Composable
fun PrescriptionScreen(nav: NavController, recordId: String? = null) {
    val vm = patientViewModel()
    val state by vm.uiState.collectAsState()
    if (recordId != null) {
        LaunchedEffect(recordId) { vm.loadPrescription(recordId) }
    }
    val saved = if (recordId != null) state.selectedPrescription else state.prescription
    if (recordId != null && saved == null) {
        LoadingPrescription(nav, state.selectedPrescriptionError) { vm.loadPrescription(recordId) }
        return
    }
    val isCurrent = recordId == null || state.prescriptions.firstOrNull()?.recordId == recordId
    // La receta la registra el optómetra en la óptica; el paciente solo la consulta.
    val rx = saved ?: run {
        NoPrescription(nav)
        return
    }
    val values =
        listOf(
            rx.sphereOD, rx.cylinderOD, rx.axisOD, rx.additionOD,
            rx.sphereOI, rx.cylinderOI, rx.axisOI, rx.additionOI,
        )

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
                    Txt(if (isCurrent) "Tu receta más reciente" else "Receta anterior", 17, weight = FontWeight.Bold, color = DeepNavy)
                    Txt(rx.issuedLabel, 13, color = Slate500)
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
                PrescriptionTable(values)
                VSpace(16.dp)
                SectionCaption("DIAGNÓSTICOS ASOCIADOS")
                VSpace(10.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rx.diagnoses.forEach { DiagnosisChip(it) }
                }
                VSpace(12.dp)
                ThinDivider()
                VSpace(10.dp)
                SectionCaption("OBSERVACIONES E INDICACIÓN")
                VSpace(8.dp)
                Txt(rx.observations, 13, color = Color(0xFF374151), lineHeight = 19.sp)
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
                        Txt("Optometrista de OptiFlow", 12, weight = FontWeight.Bold, color = Ink)
                        Txt("Receta emitida en la óptica", 10, color = Slate400)
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
        }
    }
}

@Composable
private fun PrescriptionTable(values: List<String>) {
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
                    Cell(w) { ValueCell(values[row * 4 + col]) }
                }
                Cell(1f) { Txt("—", 13, color = Slate400) }
                Cell(1f, last = true) { ValueCell(values[row * 4 + 3]) }
            }
        }
    }
}

@Composable
private fun ValueCell(value: String) {
    Txt(value, 13, weight = FontWeight.Medium, color = Ink)
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
