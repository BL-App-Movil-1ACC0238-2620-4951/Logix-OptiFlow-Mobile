package com.logix.optiflow.presentation.staff.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.presentation.navigation.Routes
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.StaffBackTopBar
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.staff.CareStep
import com.logix.optiflow.presentation.staff.ClinicalCareUiState
import com.logix.optiflow.presentation.staff.ClinicalCareViewModel
import com.logix.optiflow.presentation.staff.components.StaffButton
import com.logix.optiflow.presentation.staff.components.StaffHero
import com.logix.optiflow.presentation.staff.components.StaffInitials
import com.logix.optiflow.presentation.staff.components.StaffInput
import com.logix.optiflow.presentation.staff.components.StaffLabel
import com.logix.optiflow.presentation.staff.components.StaffScreen
import com.logix.optiflow.presentation.staff.components.StaffSoft
import com.logix.optiflow.presentation.staff.components.staffCard
import com.logix.optiflow.presentation.staff.components.staffViewModel
import com.logix.optiflow.presentation.theme.Emerald50
import com.logix.optiflow.presentation.theme.Emerald700
import com.logix.optiflow.presentation.theme.ErrorBg
import com.logix.optiflow.presentation.theme.ErrorRed
import com.logix.optiflow.presentation.theme.Inter
import com.logix.optiflow.presentation.theme.Slate300
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.StaffAccent
import com.logix.optiflow.presentation.theme.StaffBorder
import com.logix.optiflow.presentation.theme.StaffInk
import com.logix.optiflow.presentation.theme.White

private val stepLabels = listOf("Examen", "Antecedentes", "Receta")

/** Atención clínica real de una cita: examen, antecedentes y receta; al final la cita queda completada. */
@Composable
fun ClinicalCareScreen(nav: NavController, patientId: String) {
    val vm: ClinicalCareViewModel = hiltViewModel()
    val staff = staffViewModel()
    val state by vm.uiState.collectAsState()
    val onBack = { if (!vm.back()) nav.popBackStack() }
    BackHandler(enabled = state.step == CareStep.PRESCRIPTION) { vm.back() }

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Atención clínica", onBack = { onBack() }) },
    ) {
        VSpace(16.dp)
        CareHero(state)
        VSpace(18.dp)
        when {
            state.isLoading -> CenteredNote("Cargando la ficha de la cita...")
            state.loadError != null -> {
                CenteredNote(state.loadError!!)
                VSpace(14.dp)
                StaffButton("Reintentar", { vm.load() }, R.drawable.lucide_ic_clock)
            }
            else -> {
                if (state.step != CareStep.DONE) {
                    StepIndicator(state.step.ordinal)
                    VSpace(20.dp)
                }
                when (state.step) {
                    CareStep.EXAMINATION -> ExaminationStep(state, vm)
                    CareStep.HISTORY -> HistoryStep(state, vm)
                    CareStep.PRESCRIPTION -> PrescriptionStep(state, vm) { staff.refreshAgenda() }
                    CareStep.DONE -> DoneStep(
                        onRecord = { nav.navigate(Routes.staffPatient(patientId)) { popUpTo(Routes.S_AGENDA) } },
                        onAgenda = { nav.navigate(Routes.S_AGENDA) { popUpTo(Routes.S_HOME) } },
                    )
                }
                state.errorMessage?.let {
                    VSpace(14.dp)
                    ErrorBox(it)
                }
            }
        }
        VSpace(24.dp)
    }
}

@Composable
private fun CareHero(state: ClinicalCareUiState) {
    StaffHero(radius = 20.dp, padding = PaddingValues(16.dp, 18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LIcon(R.drawable.lucide_ic_clock, White, 14.dp)
            HSpace(6.dp)
            Txt(
                if (state.step == CareStep.DONE) "Atención finalizada" else "Atención en curso",
                10,
                weight = FontWeight.SemiBold,
                color = White,
                family = Inter,
                modifier = Modifier.weight(1f),
            )
            if (state.appointmentLabel.isNotEmpty()) Txt(state.appointmentLabel, 9, color = Slate300, family = Inter)
        }
        VSpace(16.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            StaffInitials(initialsOf(state.patientName.ifBlank { "Paciente" }), size = 40.dp)
            HSpace(12.dp)
            Column {
                Txt(state.patientName.ifBlank { "Paciente" }, 14, color = White, family = Inter)
                Txt("Evaluación visual", 9, color = Slate300, family = Inter)
            }
        }
    }
}

@Composable
private fun StepIndicator(current: Int) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        stepLabels.forEachIndexed { index, label ->
            val done = index < current
            val active = index == current
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (done || active) StaffAccent else StaffSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    if (done) {
                        LIcon(R.drawable.lucide_ic_check, White, 14.dp)
                    } else {
                        Txt("${index + 1}", 12, weight = FontWeight.Bold, color = if (active) White else StaffAccent, family = Inter)
                    }
                }
                VSpace(6.dp)
                Txt(label, 10, weight = if (active) FontWeight.Bold else FontWeight.Normal, color = StaffInk, family = Inter)
            }
        }
    }
}

@Composable
private fun ExaminationStep(state: ClinicalCareUiState, vm: ClinicalCareViewModel) {
    StepTitle("Registrar examen", "Abre la ficha clínica de esta cita con lo observado en la consulta.")
    VSpace(14.dp)
    StaffLabel("Observaciones", "${state.observations.length}/500")
    VSpace(8.dp)
    StaffInput(
        state.observations,
        vm::onObservationsChange,
        "Motivo de consulta, agudeza visual, hallazgos...",
        height = 120.dp,
        singleLine = false,
    )
    VSpace(20.dp)
    StaffButton(
        if (state.isSaving) "Guardando..." else "Registrar examen",
        { vm.saveExamination() },
        R.drawable.lucide_ic_check,
    )
}

@Composable
private fun HistoryStep(state: ClinicalCareUiState, vm: ClinicalCareViewModel) {
    StepTitle("Antecedentes médicos", "Sepáralos con comas. Puedes dejar vacío lo que no aplique.")
    if (state.observations.isNotBlank()) {
        VSpace(12.dp)
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(StaffSoft).padding(12.dp)) {
            Txt("OBSERVACIONES DEL EXAMEN", 8, color = StaffAccent, family = Inter, letterSpacing = 1.sp)
            VSpace(4.dp)
            Txt(state.observations, 12, color = StaffInk, family = Inter)
        }
    }
    VSpace(16.dp)
    StaffLabel("Alergias")
    VSpace(8.dp)
    StaffInput(state.allergies, vm::onAllergiesChange, "Penicilina, polen")
    VSpace(16.dp)
    StaffLabel("Enfermedades previas")
    VSpace(8.dp)
    StaffInput(state.previousConditions, vm::onPreviousConditionsChange, "Diabetes, hipertensión")
    VSpace(16.dp)
    StaffLabel("Antecedentes oculares familiares", "${state.familyHistory.length}/1000")
    VSpace(8.dp)
    StaffInput(
        state.familyHistory,
        vm::onFamilyHistoryChange,
        "Glaucoma en la madre, miopía alta en el padre...",
        height = 96.dp,
        singleLine = false,
    )
    VSpace(20.dp)
    StaffButton(
        if (state.isSaving) "Guardando..." else "Guardar antecedentes",
        { vm.saveHistory() },
        R.drawable.lucide_ic_chevron_right,
    )
}

@Composable
private fun PrescriptionStep(state: ClinicalCareUiState, vm: ClinicalCareViewModel, onCompleted: () -> Unit) {
    val form = state.prescription
    StepTitle("Receta óptica", "Esfera y cilindro en dioptrías (ej. -1.75); eje en grados de 0 a 180.")
    VSpace(14.dp)
    Column(Modifier.fillMaxWidth().staffCard().padding(14.dp)) {
        Row(Modifier.fillMaxWidth()) {
            HSpace(34.dp)
            listOf("Esfera", "Cilindro", "Eje").forEach {
                Txt(it, 10, color = Slate400, family = Inter, align = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
        VSpace(8.dp)
        EyeRow(
            "OD",
            form.sphereOD,
            form.cylinderOD,
            form.axisOD,
            { vm.onPrescriptionChange(form.copy(sphereOD = it)) },
            { vm.onPrescriptionChange(form.copy(cylinderOD = it)) },
            { vm.onPrescriptionChange(form.copy(axisOD = it)) },
        )
        VSpace(8.dp)
        EyeRow(
            "OI",
            form.sphereOS,
            form.cylinderOS,
            form.axisOS,
            { vm.onPrescriptionChange(form.copy(sphereOS = it)) },
            { vm.onPrescriptionChange(form.copy(cylinderOS = it)) },
            { vm.onPrescriptionChange(form.copy(axisOS = it)) },
        )
    }
    VSpace(16.dp)
    StaffLabel("Adición (cerca)", "Opcional · 0 a 4")
    VSpace(8.dp)
    StaffInput(form.addition, { vm.onPrescriptionChange(form.copy(addition = it.take(6))) }, "2.00", keyboardType = KeyboardType.Number)
    VSpace(16.dp)
    StaffLabel("Tratamiento", "Opcional")
    VSpace(8.dp)
    StaffInput(form.treatment, { vm.onPrescriptionChange(form.copy(treatment = it.take(255))) }, "Antirreflejo, filtro de luz azul")
    VSpace(16.dp)
    StaffLabel("Tipo de montura recomendada", "Opcional")
    VSpace(8.dp)
    StaffInput(
        form.recommendedFrameType,
        { vm.onPrescriptionChange(form.copy(recommendedFrameType = it.take(255))) },
        "Completa de acetato",
    )
    VSpace(20.dp)
    StaffButton(
        if (state.isSaving) "Guardando..." else "Guardar receta y finalizar",
        { vm.savePrescriptionAndFinish(onCompleted) },
        R.drawable.lucide_ic_check,
    )
    VSpace(10.dp)
    StaffButton(
        "Volver a antecedentes",
        { vm.back() },
        background = White,
        contentColor = StaffInk,
        border = StaffBorder,
    )
}

@Composable
private fun EyeRow(
    eye: String,
    sphere: String,
    cylinder: String,
    axis: String,
    onSphere: (String) -> Unit,
    onCylinder: (String) -> Unit,
    onAxis: (String) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Txt(eye, 12, weight = FontWeight.Bold, color = StaffAccent, family = Inter, modifier = Modifier.width(28.dp))
        Box(Modifier.weight(1f)) { StaffInput(sphere, { onSphere(it.take(6)) }, "0.00", keyboardType = KeyboardType.Number, height = 44.dp) }
        Box(Modifier.weight(1f)) { StaffInput(cylinder, { onCylinder(it.take(6)) }, "0.00", keyboardType = KeyboardType.Number, height = 44.dp) }
        Box(Modifier.weight(1f)) { StaffInput(axis, { onAxis(it.filter(Char::isDigit).take(3)) }, "0", keyboardType = KeyboardType.Number, height = 44.dp) }
    }
}

@Composable
private fun DoneStep(onRecord: () -> Unit, onAgenda: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Emerald50).padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(Emerald700), contentAlignment = Alignment.Center) {
            LIcon(R.drawable.lucide_ic_check, White, 24.dp)
        }
        VSpace(12.dp)
        Txt("Atención finalizada", 16, weight = FontWeight.Bold, color = Emerald700, family = Inter)
        VSpace(6.dp)
        Txt(
            "La ficha clínica y la receta quedaron guardadas y la cita figura como completada.",
            12,
            color = StaffInk,
            family = Inter,
            align = TextAlign.Center,
        )
    }
    VSpace(18.dp)
    StaffButton("Ver ficha del paciente", onRecord, R.drawable.lucide_ic_user)
    VSpace(10.dp)
    StaffButton("Volver a la agenda", onAgenda, R.drawable.lucide_ic_calendar, background = White, contentColor = StaffInk, border = StaffBorder)
}

@Composable
private fun StepTitle(title: String, subtitle: String) {
    Txt(title, 16, weight = FontWeight.Bold, color = StaffInk, family = Inter)
    VSpace(6.dp)
    Txt(subtitle, 12, color = StaffAccent, family = Inter)
}

@Composable
private fun CenteredNote(text: String) {
    Txt(text, 13, color = Slate400, family = Inter, align = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 30.dp))
}

@Composable
private fun ErrorBox(text: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(ErrorBg).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Txt(text, 12, color = ErrorRed, family = Inter, modifier = Modifier.weight(1f))
    }
}

private fun initialsOf(name: String): String =
    name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
