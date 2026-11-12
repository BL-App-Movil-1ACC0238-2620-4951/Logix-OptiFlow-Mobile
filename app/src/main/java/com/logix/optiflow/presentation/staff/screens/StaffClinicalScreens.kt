package com.logix.optiflow.presentation.staff.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.data.demo.ProductionStage
import com.logix.optiflow.domain.model.STORE_ZONE
import com.logix.optiflow.presentation.navigation.Routes
import com.logix.optiflow.presentation.shared.OfflineViewModel
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.StaffBackTopBar
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.staff.StaffFeature
import com.logix.optiflow.presentation.staff.StaffPatientRecordViewModel
import com.logix.optiflow.presentation.staff.can
import com.logix.optiflow.presentation.staff.components.StaffButton
import com.logix.optiflow.presentation.staff.components.StaffChip
import com.logix.optiflow.presentation.staff.components.StaffHero
import com.logix.optiflow.presentation.staff.components.StaffIconTile
import com.logix.optiflow.presentation.staff.components.StaffIndigoText
import com.logix.optiflow.presentation.staff.components.StaffInitials
import com.logix.optiflow.presentation.staff.components.StaffInput
import com.logix.optiflow.presentation.staff.components.StaffLabel
import com.logix.optiflow.presentation.staff.components.StaffPill
import com.logix.optiflow.presentation.staff.components.StaffScreen
import com.logix.optiflow.presentation.staff.components.StaffSectionTitle
import com.logix.optiflow.presentation.staff.components.StaffSelect
import com.logix.optiflow.presentation.staff.components.StaffSoft
import com.logix.optiflow.presentation.staff.components.staffCard
import com.logix.optiflow.presentation.staff.components.staffViewModel
import com.logix.optiflow.presentation.staff.NewAppointmentViewModel
import com.logix.optiflow.presentation.theme.Inter
import com.logix.optiflow.presentation.theme.Lavender
import com.logix.optiflow.presentation.theme.Slate300
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.StaffAccent
import com.logix.optiflow.presentation.theme.StaffBorder
import com.logix.optiflow.presentation.theme.StaffChip
import com.logix.optiflow.presentation.theme.StaffHeader
import com.logix.optiflow.presentation.theme.StaffInk
import com.logix.optiflow.presentation.theme.White
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val AccentLight = Color(0xFF60A5FA)

@Composable
fun NewPatientScreen(nav: NavController) {
    val vm = staffViewModel()
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var dni by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf("") }
    var saving by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Nuevo Cliente", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(16.dp)
        StaffHero(padding = PaddingValues(20.dp, 22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StaffIconTile(R.drawable.lucide_ic_file_text, size = 36.dp, background = White, radius = 10.dp)
                HSpace(14.dp)
                Column {
                    Txt("FICHA DIGITAL", 9, color = AccentLight, family = Inter, letterSpacing = 1.sp)
                    VSpace(6.dp)
                    Txt("Registro rápido de paciente", 18, color = White, family = Inter)
                    VSpace(6.dp)
                    Txt("El paciente podrá entrar a la app con su correo y su DNI como contraseña.", 9, color = White.copy(alpha = 0.85f), family = Inter)
                }
            }
        }
        VSpace(18.dp)
        StaffLabel("Nombre completo")
        VSpace(10.dp)
        StaffInput(name, { name = it.take(80) }, "Camila Paz Morales", R.drawable.lucide_ic_user)
        VSpace(20.dp)
        StaffLabel("Correo electrónico")
        VSpace(10.dp)
        StaffInput(email, { email = it.trim() }, "camila@correo.com", R.drawable.lucide_ic_mail, KeyboardType.Email)
        VSpace(20.dp)
        StaffLabel("Teléfono móvil")
        VSpace(10.dp)
        StaffInput(phone, { phone = it.take(20) }, "987 654 321", R.drawable.lucide_ic_phone, KeyboardType.Phone)
        VSpace(20.dp)
        StaffLabel("DNI", "Será su contraseña temporal")
        VSpace(10.dp)
        StaffInput(dni, { dni = it.filter(Char::isDigit).take(8) }, "45879632", R.drawable.lucide_ic_credit_card, KeyboardType.Number)
        VSpace(20.dp)
        StaffLabel("Fecha de nacimiento", "Opcional · dd/mm/aaaa")
        VSpace(10.dp)
        StaffInput(birthDate, { birthDate = it.take(10) }, "24/05/1990", R.drawable.lucide_ic_calendar, KeyboardType.Number)
        VSpace(28.dp)
        StaffButton(if (saving) "Guardando..." else "Guardar paciente", onClick = {
            val birth = parseBirthDate(birthDate)
            val error =
                when {
                    name.trim().length < 2 -> "Ingresa el nombre del paciente"
                    !EMAIL_REGEX.matches(email) -> "Ingresa un correo válido"
                    phone.isBlank() -> "Ingresa el teléfono del paciente"
                    dni.length != 8 -> "El DNI debe tener 8 dígitos"
                    birthDate.isNotBlank() && birth == null -> "La fecha de nacimiento no es válida"
                    else -> null
                }
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            } else if (!saving) {
                saving = true
                vm.registerPatient(name.trim(), email, phone.trim(), dni, birth) { patientId, message ->
                    saving = false
                    if (patientId != null) {
                        Toast.makeText(context, "Paciente registrado", Toast.LENGTH_SHORT).show()
                        nav.navigate(Routes.staffPatient(patientId)) { popUpTo(Routes.S_PATIENTS) }
                    } else {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }, icon = R.drawable.lucide_ic_check)
        VSpace(14.dp)
        Txt(
            "Cancelar",
            13,
            weight = FontWeight.Bold,
            color = StaffAccent,
            family = Inter,
            modifier = Modifier.align(Alignment.CenterHorizontally).tap { nav.popBackStack() },
        )
        VSpace(24.dp)
    }
}

private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

/** "24/05/1990" → "1990-05-24"; null si no es una fecha pasada válida. */
private fun parseBirthDate(text: String): String? =
    runCatching {
        LocalDate.parse(text.trim(), DateTimeFormatter.ofPattern("d/M/uuuu"))
    }.getOrNull()?.takeIf { it.isBefore(LocalDate.now(STORE_ZONE)) }?.toString()

@Composable
fun NewAppointmentScreen(nav: NavController, patientId: String? = null) {
    val staffVm = staffViewModel()
    val staff by staffVm.uiState.collectAsState()
    val vm: NewAppointmentViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    var choosingPatient by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(staff.storeId) { vm.load(staff.storeId, patientId) }
    LaunchedEffect(state.booked) {
        if (state.booked) {
            Toast.makeText(context, "Cita agendada.", Toast.LENGTH_SHORT).show()
            // La agenda muestra el día de la cita recién agendada.
            state.day?.let { staffVm.refreshAgenda(it) }
            nav.popBackStack()
        }
    }
    val patient = staff.patients.firstOrNull { it.id == state.patientId }

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Nueva cita", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(16.dp)
        StaffHero(radius = 20.dp, padding = PaddingValues(18.dp, 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StaffIconTile(R.drawable.lucide_ic_calendar, size = 36.dp, background = White, radius = 10.dp)
                HSpace(14.dp)
                Column {
                    Txt("AGENDAMIENTO · PASO ÚNICO", 9, color = AccentLight, family = Inter, letterSpacing = 1.sp)
                    VSpace(4.dp)
                    Txt("Datos de la cita", 18, color = White, family = Inter)
                }
            }
        }
        VSpace(18.dp)
        StaffLabel("Paciente", "Registrado en OptiFlow")
        VSpace(8.dp)
        StaffSelect(
            patient?.fullName ?: "Elegir paciente",
            if (patient == null) R.drawable.lucide_ic_user else null,
            onClick = { choosingPatient = !choosingPatient },
            leading = patient?.let { p -> { StaffInitials(p.initials, size = 28.dp, fontSize = 9) } },
        )
        if (choosingPatient) {
            VSpace(8.dp)
            if (staff.patients.isEmpty()) {
                EmptyCard("Todavía no hay pacientes registrados.")
            } else {
                Column(Modifier.fillMaxWidth().staffCard()) {
                    staff.patients.forEach { p ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .tap {
                                    vm.selectPatient(p.id)
                                    choosingPatient = false
                                }.padding(14.dp, 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StaffInitials(p.initials, size = 28.dp, fontSize = 9)
                            HSpace(12.dp)
                            Txt(p.fullName, 13, color = StaffInk, family = Inter)
                        }
                    }
                }
            }
        }
        VSpace(22.dp)
        StaffSectionTitle("Fecha", "Turnos libres de tu óptica")
        VSpace(12.dp)
        when {
            state.isLoading -> EmptyCard("Cargando horarios...")
            state.slots.isEmpty() ->
                EmptyCard("No hay turnos libres. El administrador puede abrirlos en Más > Horarios de atención.")
            else -> {
                ChoiceGrid(state.days, state.day, { it.format(DAY_CHIP) }) { vm.selectDay(it) }
                VSpace(12.dp)
                StaffSectionTitle("Hora", "Selecciona horario")
                VSpace(12.dp)
                val hours = state.daySlots.associate { it.id to it.startDateTime.atZone(STORE_ZONE).toLocalTime().format(HOUR_CHIP) }
                ChoiceGrid(hours.keys.toList(), state.slotId, { hours[it].orEmpty() }) { vm.selectSlot(it) }
            }
        }
        state.errorMessage?.let {
            VSpace(8.dp)
            Txt(it, 12, color = Color(0xFFDC2626), family = Inter)
        }
        VSpace(14.dp)
        StaffButton(
            if (state.isSaving) "Agendando..." else "Confirmar cita",
            onClick = { if (!state.isSaving) vm.submit() },
            icon = R.drawable.lucide_ic_check,
        )
        VSpace(24.dp)
    }
}

private val DAY_CHIP = DateTimeFormatter.ofPattern("EEE d MMM", Locale.forLanguageTag("es-PE"))
private val HOUR_CHIP = DateTimeFormatter.ofPattern("HH:mm")

/** Opciones en filas de 3, como el selector de horas del prototipo. */
@Composable
private fun <T> ChoiceGrid(options: List<T>, selected: T?, label: (T) -> String, onSelect: (T) -> Unit) {
    options.chunked(3).forEach { chunk ->
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            chunk.forEach { option ->
                val isSelected = option == selected
                Box(
                    Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) StaffAccent else White)
                        .border(1.dp, if (isSelected) StaffAccent else StaffBorder, RoundedCornerShape(10.dp))
                        .tap { onSelect(option) },
                    contentAlignment = Alignment.Center,
                ) {
                    Txt(label(option), 13, color = if (isSelected) White else StaffInk, family = Inter)
                }
            }
            repeat(3 - chunk.size) { Spacer(Modifier.weight(1f)) }
        }
        VSpace(10.dp)
    }
}

@Composable
fun StaffPatientRecordScreen(nav: NavController, id: String) {
    val vm = staffViewModel()
    val state by vm.uiState.collectAsState()
    val recordVm: StaffPatientRecordViewModel = hiltViewModel()
    val record by recordVm.uiState.collectAsState()
    val context = LocalContext.current
    val patient = state.patients.firstOrNull { it.id == id }
    if (patient == null) {
        LaunchedEffect(id) { nav.popBackStack() }
        return
    }

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = {
            StaffBackTopBar("Ficha del Paciente", onBack = { nav.popBackStack() })
        },
    ) {
        VSpace(16.dp)
        StaffHero(radius = 20.dp, padding = PaddingValues(16.dp, 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StaffInitials(patient.initials, size = 64.dp, fontSize = 16)
                HSpace(14.dp)
                Column {
                    StaffPill("Paciente activo", background = Color(0x3310B981), color = Color(0xFFA7F3D0))
                    VSpace(4.dp)
                    Txt(patient.fullName, 20, weight = FontWeight.Bold, color = White, family = Inter, letterSpacing = (-0.6).sp)
                    Txt("ID #${patient.ficha}", 10, color = Slate300, family = Inter)
                }
            }
            VSpace(22.dp)
            Row {
                Column(Modifier.weight(1f)) {
                    Txt("TELÉFONO", 8, color = Slate300, family = Inter)
                    VSpace(6.dp)
                    Txt(patient.phone, 15, color = White, family = Inter)
                }
                Column(Modifier.weight(1f)) {
                    Txt("ÚLTIMA VISITA", 8, color = Slate300, family = Inter)
                    VSpace(6.dp)
                    Txt(record.lastVisit ?: "Sin visitas", 15, color = White, family = Inter)
                }
            }
            VSpace(18.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StaffButton("Llamar", {}, R.drawable.lucide_ic_phone, background = White, contentColor = Color(0xFF0013C1), height = 46.dp, modifier = Modifier.weight(1f))
                StaffButton("Mensaje", {}, R.drawable.lucide_ic_mail, background = White, contentColor = Color(0xFF0013C1), height = 46.dp, modifier = Modifier.weight(1f))
            }
        }
        VSpace(24.dp)
        StaffSectionTitle("Última prescripción")
        VSpace(12.dp)
        val rx = record.prescription
        if (rx == null) {
            EmptyCard(if (record.isLoading) "Cargando..." else "Aún no tiene recetas registradas.")
        } else {
            Column(Modifier.fillMaxWidth().staffCard().padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LIcon(R.drawable.lucide_ic_eye, StaffAccent, 26.dp)
                    HSpace(14.dp)
                    Column(Modifier.weight(1f)) {
                        Txt("Última Prescripción", 18, color = StaffInk, family = Inter)
                        Txt(rx.dateLabel, 8, color = StaffInk, family = Inter)
                    }
                }
                VSpace(14.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EyeBox("Ojo derecho", rx.rightEye, rx.rightDetail, Modifier.weight(1f))
                    EyeBox("Ojo izquierdo", rx.leftEye, rx.leftDetail, Modifier.weight(1f))
                }
                rx.note?.let { note ->
                    VSpace(8.dp)
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF2FF)).padding(12.dp)) {
                        Txt(note, 13, color = StaffAccent, family = Inter)
                    }
                }
            }
        }
        VSpace(24.dp)
        StaffSectionTitle("Próxima cita")
        VSpace(12.dp)
        val next = record.nextAppointment
        if (next == null) {
            EmptyCard(if (record.isLoading) "Cargando..." else "No tiene citas próximas.")
        } else {
            Row(
                Modifier.fillMaxWidth().staffCard().tap { nav.navigate(Routes.S_AGENDA) }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    Modifier.size(56.dp, 60.dp).clip(RoundedCornerShape(12.dp)).background(StaffChip),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Txt(next.day, 20, color = StaffInk, family = Inter)
                    Txt(next.month, 11, color = StaffInk, family = Inter)
                }
                HSpace(12.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    StaffPill(next.statusLabel)
                    Txt("Evaluación visual completa", 13, color = StaffInk, family = Inter)
                    Txt(next.timeLabel, 10, color = Slate400, family = Inter)
                }
                LIcon(R.drawable.lucide_ic_chevron_right, StaffInk, 20.dp)
            }
            // Quien agenda (asesor, optometrista o admin) también puede cancelar.
            if (state.role.can(StaffFeature.AGENDA)) {
                VSpace(4.dp)
                Box(
                    Modifier.fillMaxWidth().tap {
                        recordVm.cancelNextAppointment { day, error ->
                            if (day != null) vm.refreshAgenda(day)
                            Toast.makeText(context, error ?: "Cita cancelada.", Toast.LENGTH_SHORT).show()
                        }
                    }.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Txt(if (record.isCancelling) "Cancelando..." else "Cancelar cita", 13, color = StaffAccent, family = Inter)
                }
            }
        }
        VSpace(24.dp)
        val lensOrder = record.lensOrder
        StaffSectionTitle("Proceso de sus lentes", lensOrder?.let { "Orden ${it.code}" }) {
            lensOrder?.let { nav.navigate(Routes.workOrder(it.id)) }
        }
        VSpace(12.dp)
        if (lensOrder == null) {
            EmptyCard(if (record.isLoading) "Cargando…" else "El paciente no tiene órdenes de trabajo.")
        } else {
            Column(Modifier.fillMaxWidth().staffCard().tap { nav.navigate(Routes.workOrder(lensOrder.id)) }.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StaffIconTile(R.drawable.lucide_ic_settings, size = 36.dp, background = StaffSoft)
                    HSpace(12.dp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        StaffPill(lensOrder.statusLabel, icon = R.drawable.lucide_ic_clock)
                        Txt(lensOrder.detail, 12, color = StaffInk, family = Inter)
                    }
                    LIcon(R.drawable.lucide_ic_chevron_right, StaffInk, 20.dp)
                }
                VSpace(14.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(5) { index ->
                        val color = if (index < lensOrder.step) StaffAccent else StaffChip
                        Box(Modifier.weight(1f).height(5.dp).clip(CircleShape).background(color))
                    }
                }
                VSpace(8.dp)
                Txt(lensOrder.dueLabel, 9, color = StaffAccent, family = Inter)
            }
        }
        if (state.role.can(StaffFeature.QUOTES)) {
            // Se relee al volver de la venta, para que desaparezca la que se acaba de cerrar.
            LifecycleResumeEffect(id) {
                recordVm.loadOpenSales()
                onPauseOrDispose {}
            }
            if (record.openSales.isNotEmpty()) {
                VSpace(24.dp)
                StaffSectionTitle("Ventas por cerrar")
                VSpace(12.dp)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    record.openSales.forEach { sale ->
                        Row(
                            Modifier.fillMaxWidth().staffCard().tap { nav.navigate(Routes.sale(sale.id, patient.id)) }.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StaffIconTile(R.drawable.lucide_ic_file_text, size = 36.dp, background = StaffSoft)
                            HSpace(12.dp)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                StaffPill(sale.statusLabel, icon = R.drawable.lucide_ic_clock)
                                Txt(sale.detail, 12, color = StaffInk, family = Inter)
                            }
                            LIcon(R.drawable.lucide_ic_chevron_right, StaffInk, 20.dp)
                        }
                    }
                }
            }
        }
        VSpace(18.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StaffButton("Nueva cita", { nav.navigate(Routes.newAppointment(patient.id)) }, R.drawable.lucide_ic_calendar, modifier = Modifier.weight(1f))
            if (state.role.can(StaffFeature.CLINICAL)) {
            StaffButton(
                "Iniciar atención",
                {
                    if (next?.canStartCare == true) {
                        nav.navigate(Routes.clinicalCare(next.appointmentId, patient.id))
                    } else {
                        Toast.makeText(context, "El paciente no tiene una cita para hoy", Toast.LENGTH_SHORT).show()
                    }
                },
                R.drawable.lucide_ic_plus,
                background = White,
                contentColor = StaffInk,
                border = StaffBorder,
                modifier = Modifier.weight(1f),
            )
            }
        }
        if (state.role.can(StaffFeature.QUOTES)) {
            VSpace(10.dp)
            StaffButton("Crear cotización", { nav.navigate(Routes.quote(patient.id)) }, R.drawable.lucide_ic_file_text)
        }
        VSpace(24.dp)
    }
}

@Composable
private fun EmptyCard(text: String) {
    Box(Modifier.fillMaxWidth().staffCard().padding(16.dp)) {
        Txt(text, 12, color = Slate400, family = Inter)
    }
}

@Composable
private fun EyeBox(label: String, value: String, detail: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF2FF)).padding(12.dp)) {
        Txt(label, 8, color = StaffInk, family = Inter)
        VSpace(6.dp)
        Txt(value, 17, color = StaffInk, family = Inter)
        VSpace(4.dp)
        Txt(detail, 8, color = StaffAccent, family = Inter)
    }
}

@Composable
fun ProductionScreen(nav: NavController) {
    val vm = staffViewModel()
    val state by vm.uiState.collectAsState()
    var stage by rememberSaveable { mutableIntStateOf(ProductionStage.IN_WORKSHOP.ordinal) }
    val orders = state.productionOrders.filter { it.stage.ordinal == stage }
    val stageColors = listOf(Color(0xFFAAB6DD), Color(0xFF1E3D79), White, White, White)

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Producción", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(20.dp)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProductionStage.entries.forEachIndexed { index, item ->
                val selected = index == stage
                val count = state.productionOrders.count { it.stage == item }
                Column(
                    Modifier
                        .width(94.dp)
                        .height(58.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Color(0xFF1E3D79) else if (index == 0) stageColors[0] else White)
                        .border(1.dp, if (selected) Color(0xFF1E3D79) else Lavender, RoundedCornerShape(12.dp))
                        .tap { stage = index },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Txt("$count", 13, color = if (selected) White else StaffHeader, family = Inter)
                    VSpace(4.dp)
                    Txt(item.label, 8, color = if (selected) White else StaffInk, family = Inter)
                }
            }
        }
        VSpace(26.dp)
        Txt(
            "${ProductionStage.entries[stage].label} · ${orders.size}",
            18,
            weight = FontWeight.Bold,
            color = StaffInk,
            family = Inter,
            modifier = Modifier.padding(start = 4.dp),
        )
        VSpace(14.dp)
        if (orders.isEmpty()) Txt("No hay órdenes en esta etapa.", 12, color = Slate400, family = Inter)
        orders.forEach { order ->
            Column(Modifier.fillMaxWidth().staffCard().tap { order.id?.let { nav.navigate(Routes.workOrder(it)) } }.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (order.needsAttention) {
                        StaffPill("Requiere atención", icon = R.drawable.lucide_ic_bell)
                    } else {
                        StaffPill(ProductionStage.entries[stage].label, icon = R.drawable.lucide_ic_clock)
                    }
                    androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                    Txt(order.code, 9, color = StaffInk, family = Inter)
                }
                VSpace(12.dp)
                Txt(order.patient, 14, color = StaffInk, family = Inter)
                VSpace(4.dp)
                Txt(order.detail, 9, color = StaffIndigoText, family = Inter)
                VSpace(12.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LIcon(R.drawable.lucide_ic_calendar, StaffAccent, 14.dp)
                    HSpace(8.dp)
                    Txt(order.due, 10, color = StaffAccent, family = Inter)
                }
            }
            VSpace(10.dp)
        }
        if (state.productionFromApi) {
            val offline by hiltViewModel<OfflineViewModel>().savedDataFrom.collectAsState()
            VSpace(6.dp)
            Txt(
                // US10: sin conexión el tablero guardado es de solo lectura.
                if (offline != null) "Tablero guardado en solo lectura: para cambiar una orden necesitas conexión." else "Órdenes sincronizadas con el backend.",
                10,
                color = Slate400,
                family = Inter,
            )
        }
        VSpace(24.dp)
    }
}
