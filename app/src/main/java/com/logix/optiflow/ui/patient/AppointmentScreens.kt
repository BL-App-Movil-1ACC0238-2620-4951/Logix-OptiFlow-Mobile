package com.logix.optiflow.ui.patient

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.data.remote.dto.TimeSlotDto
import com.logix.optiflow.ui.components.Divider
import com.logix.optiflow.ui.components.HSpace
import com.logix.optiflow.ui.components.IconTile
import com.logix.optiflow.ui.components.LIcon
import com.logix.optiflow.ui.components.PatientDarkTopBar
import com.logix.optiflow.ui.components.PatientTab
import com.logix.optiflow.ui.components.Pill
import com.logix.optiflow.ui.components.SegmentChip
import com.logix.optiflow.ui.components.SolidButton
import com.logix.optiflow.ui.components.Txt
import com.logix.optiflow.ui.components.VSpace
import com.logix.optiflow.ui.components.card
import com.logix.optiflow.ui.components.tap
import com.logix.optiflow.ui.navigation.Routes
import com.logix.optiflow.ui.theme.DeepNavy
import com.logix.optiflow.ui.theme.Electric
import com.logix.optiflow.ui.theme.InfoBanner
import com.logix.optiflow.ui.theme.Lavender
import com.logix.optiflow.ui.theme.NavyBlue
import com.logix.optiflow.ui.theme.SkyTint
import com.logix.optiflow.ui.theme.Slate400
import com.logix.optiflow.ui.theme.Slate500
import com.logix.optiflow.ui.theme.White
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val appointmentTabs = listOf("Próximas", "Anteriores", "Canceladas")

@Composable
fun MyAppointmentsScreen(nav: NavController) {
    val vm = patientViewModel()
    val state by vm.uiState.collectAsState()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val items =
        state.appointments.filter {
            when (tab) {
                0 -> !it.isPast && it.status != AppointmentStatus.CANCELLED
                1 -> it.isPast && it.status != AppointmentStatus.CANCELLED
                else -> it.status == AppointmentStatus.CANCELLED
            }
        }

    PatientScreen(
        nav = nav,
        tab = PatientTab.APPOINTMENTS,
        topBar = {
            PatientDarkTopBar(
                "Mis citas",
                onBack = { nav.popBackStack() },
                below = {
                    Row(
                        Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 22.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        appointmentTabs.forEachIndexed { index, label ->
                            SegmentChip(
                                text = label,
                                selected = tab == index,
                                onClick = { tab = index },
                                selectedBg = SkyTint,
                                selectedText = DeepNavy,
                                unselectedText = DeepNavy,
                                fontSize = 14,
                                height = 38.dp,
                                weight = FontWeight.Medium,
                            )
                        }
                    }
                },
            )
        },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
        ) {
            VSpace(26.dp)
            Row(
                Modifier
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(SkyTint)
                    .tap { nav.navigate(Routes.book()) }
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LIcon(R.drawable.lucide_ic_plus, DeepNavy, 16.dp)
                HSpace(10.dp)
                Txt("Reservar cita", 14, weight = FontWeight.Medium, color = DeepNavy)
            }
            VSpace(18.dp)
            if (items.isEmpty()) {
                Txt(
                    when (tab) {
                        0 -> "No tienes citas próximas."
                        1 -> "Aún no tienes citas anteriores."
                        else -> "No tienes citas canceladas."
                    },
                    14,
                    color = Slate500,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
            items.groupBy { it.monthName }.forEach { (month, group) ->
                Txt(month, 16, weight = FontWeight.SemiBold, color = DeepNavy)
                VSpace(12.dp)
                group.forEach { appt ->
                    AppointmentCard(appt) { nav.navigate(Routes.appointment(appt.id)) }
                    VSpace(18.dp)
                }
            }
        }
    }
}

@Composable
private fun AppointmentCard(appointment: AppointmentUi, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .card(radius = 16.dp, border = Lavender, elevation = 2.dp)
            .tap(onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DateBadge(appointment.day, appointment.month, width = 56.dp, height = 64.dp, background = Color(0xFFEEF2FF))
        HSpace(16.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            StatusCapsule(appointment.status)
            Txt(appointment.title, 16, weight = FontWeight.Bold, color = DeepNavy, modifier = Modifier.fillMaxWidth(0.7f))
            Txt(
                "${appointment.storeName} · ${appointment.time}",
                13,
                color = Slate500,
                modifier = Modifier.fillMaxWidth(0.7f),
            )
        }
        LIcon(R.drawable.lucide_ic_chevron_right, Slate500, 20.dp)
    }
}

@Composable
fun AppointmentDetailScreen(nav: NavController, id: String) {
    val vm = patientViewModel()
    val state by vm.uiState.collectAsState()
    val appointment = state.appointments.firstOrNull { it.id == id } ?: PatientViewModel.demoAppointments().first()
    val context = LocalContext.current

    PatientScreen(
        nav = nav,
        tab = PatientTab.APPOINTMENTS,
        topBar = { PatientDarkTopBar("Detalle de cita", onBack = { nav.popBackStack() }) },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 34.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            VSpace(22.dp)
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFDFE7FC))
                    .border(1.dp, Color(0xFFC3D3F5), RoundedCornerShape(20.dp))
                    .padding(16.dp),
            ) {
                Box(Modifier.clip(CircleShape).background(White).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LIcon(
                            if (appointment.status == AppointmentStatus.CONFIRMED) R.drawable.lucide_ic_check else R.drawable.lucide_ic_clock,
                            DeepNavy,
                            13.dp,
                        )
                        HSpace(6.dp)
                        Txt(appointment.status.label, 12, weight = FontWeight.Bold, color = DeepNavy)
                    }
                }
                VSpace(12.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        Modifier.size(width = 56.dp, height = 54.dp).clip(RoundedCornerShape(12.dp)).background(White),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Txt(appointment.day, 20, weight = FontWeight.ExtraBold, color = DeepNavy)
                        Txt(appointment.month, 10, weight = FontWeight.Bold, color = Slate500)
                    }
                    HSpace(16.dp)
                    Column {
                        Txt(appointment.title, 18, weight = FontWeight.Bold, color = DeepNavy)
                        Txt("${appointment.weekday} · ${appointment.time} a ${appointment.endTime}", 13, color = Slate500)
                    }
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .card(radius = 20.dp, border = Lavender)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                DetailLine(R.drawable.lucide_ic_map_pin, "Ubicación", appointment.storeName, appointment.storeAddress)
                VSpace(14.dp)
                Divider(Lavender)
                VSpace(14.dp)
                DetailLine(R.drawable.lucide_ic_user, "Profesional", appointment.professional, null)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(InfoBanner)
                    .border(1.dp, Color(0xFFC3D3F5), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconTile(R.drawable.lucide_ic_bell, Color(0xFFC7D5F7), Electric, size = 40.dp, iconSize = 18.dp, radius = 12.dp)
                HSpace(12.dp)
                Column {
                    Txt("Recordatorio activado", 14, weight = FontWeight.Bold, color = DeepNavy)
                    Txt("Te avisaremos 24 horas antes.", 12, color = Slate500)
                }
            }
            VSpace(6.dp)
            if (appointment.status != AppointmentStatus.CANCELLED) {
                SolidButton(
                    text = "Reprogramar cita",
                    onClick = { nav.navigate(Routes.reschedule(appointment.id)) },
                    background = White,
                    contentColor = Electric,
                    border = SkyTint,
                    height = 48.dp,
                    fontSize = 15,
                    leadingIcon = R.drawable.lucide_ic_pencil,
                )
                Txt(
                    "Cancelar cita",
                    14,
                    weight = FontWeight.Bold,
                    color = Electric,
                    align = TextAlign.Center,
                    modifier =
                        Modifier.fillMaxWidth().padding(vertical = 8.dp).tap {
                            vm.cancelAppointment(appointment.id)
                            Toast.makeText(context, "Cita cancelada", Toast.LENGTH_SHORT).show()
                            nav.popBackStack()
                        },
                )
            }
        }
    }
}

@Composable
private fun DetailLine(icon: Int, label: String, value: String, extra: String?) {
    Row {
        LIcon(icon, Electric, 18.dp, Modifier.padding(top = 4.dp))
        HSpace(16.dp)
        Column {
            Txt(label, 12, color = Slate400)
            Txt(value, 15, weight = FontWeight.Bold, color = DeepNavy)
            if (!extra.isNullOrBlank()) {
                VSpace(4.dp)
                Txt(extra, 12, color = Slate500)
            }
        }
    }
}

private val services = listOf("Examen de vista completo", "Control de lentes", "Adaptación de lentes de contacto")
private val rescheduleTimes = listOf("09:00", "10:30", "12:00", "14:30", "16:00", "17:00")
private val FieldBorder = Color(0xFFA8BCD9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RescheduleScreen(nav: NavController, id: String) {
    val vm = patientViewModel()
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    val appointment = vm.appointment(id)
    // Con una cita real y ópticas del API se usan los horarios disponibles; si no, la vista de demostración.
    val live = appointment?.fromApi == true && state.stores.isNotEmpty()
    var service by remember { mutableIntStateOf(0) }
    var store by remember {
        mutableIntStateOf(state.stores.indexOfFirst { it.id == appointment?.storeId }.coerceAtLeast(0))
    }
    var time by remember { mutableIntStateOf(1) }
    var date by remember { mutableStateOf<Long?>(null) }
    var showPicker by remember { mutableStateOf(false) }
    var slots by remember { mutableStateOf<List<TimeSlotDto>>(emptyList()) }
    var saving by remember { mutableStateOf(false) }
    val storeNames = state.stores.map { it.name }.ifEmpty { listOf("Sucursal Centro", "Sucursal Norte", "Sucursal Providencia") }
    LaunchedEffect(live, store) {
        if (live) {
            slots = vm.availableSlots(state.stores[store.coerceAtMost(state.stores.lastIndex)].id)
            time = -1
        }
    }
    val chosenDay = date?.let { Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate() }
    LaunchedEffect(chosenDay) { if (live) time = -1 }
    val daySlots =
        slots.filter { slot -> chosenDay == null || slot.startDateTime.atZone(ZoneId.systemDefault()).toLocalDate() == chosenDay }
    val timeLabels =
        if (live) {
            daySlots.map { DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault()).format(it.startDateTime) }
        } else {
            rescheduleTimes
        }

    PatientScreen(
        nav = nav,
        tab = PatientTab.APPOINTMENTS,
        topBar = { PatientDarkTopBar("Detalle de cita", onBack = { nav.popBackStack() }) },
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .card(radius = 24.dp, elevation = 4.dp)
                    .padding(horizontal = 24.dp, vertical = 22.dp),
            ) {
                Txt("Reprograma cita", 22, weight = FontWeight.ExtraBold, color = NavyBlue)
                VSpace(10.dp)
                Txt("Elige el servicio y el horario que mejor te convenga.", 14, color = Slate500)
                VSpace(18.dp)
                FormLabel("Servicio")
                Dropdown(services, service) { service = it }
                VSpace(18.dp)
                FormLabel("Óptica")
                Dropdown(storeNames, store.coerceAtMost(storeNames.lastIndex)) {
                    store = it
                    time = -1
                }
                VSpace(18.dp)
                FormLabel("Fecha")
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF4F8FF))
                        .border(1.dp, FieldBorder, RoundedCornerShape(8.dp))
                        .tap { showPicker = true }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    date?.let {
                        val text =
                            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "PE"))
                                .withZone(ZoneId.of("UTC"))
                                .format(Instant.ofEpochMilli(it))
                        Txt(text.replaceFirstChar { c -> c.titlecase() }, 14, color = DeepNavy)
                    }
                }
                VSpace(18.dp)
                FormLabel("Horarios disponibles")
                if (live && timeLabels.isEmpty()) {
                    Txt("No hay horarios disponibles para esta fecha.", 13, color = Slate500)
                    VSpace(8.dp)
                }
                timeLabels.chunked(3).forEachIndexed { row, chunk ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        chunk.forEachIndexed { col, label ->
                            val index = row * 3 + col
                            val selected = index == time
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) Color(0xFFBFDBFE) else White)
                                    .border(1.dp, if (selected) Color(0xFFBFDBFE) else FieldBorder, RoundedCornerShape(8.dp))
                                    .tap { time = index },
                                contentAlignment = Alignment.Center,
                            ) {
                                Txt(label, 15, color = if (selected) White else DeepNavy)
                            }
                        }
                    }
                    VSpace(8.dp)
                }
                VSpace(10.dp)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFDDEFFB))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LIcon(R.drawable.lucide_ic_calendar_clock, Color(0xFF0E6A93), 20.dp)
                    HSpace(12.dp)
                    Txt("Recibirás un recordatorio 24 horas antes de tu cita.", 12, color = Color(0xFF0E5A80))
                }
                VSpace(18.dp)
                SolidButton(
                    text = "Confirmar reserva",
                    onClick = {
                        val slot = daySlots.getOrNull(time)
                        when {
                            date == null -> Toast.makeText(context, "Selecciona una fecha", Toast.LENGTH_SHORT).show()
                            !live -> {
                                Toast.makeText(context, "Solicitud de reprogramación enviada", Toast.LENGTH_SHORT).show()
                                nav.popBackStack(Routes.P_APPOINTMENTS, inclusive = false)
                            }
                            slot == null -> Toast.makeText(context, "Selecciona un horario", Toast.LENGTH_SHORT).show()
                            !saving -> {
                                saving = true
                                vm.reschedule(id, state.stores[store].id, slot.id) { error ->
                                    saving = false
                                    Toast.makeText(context, error ?: "Cita reprogramada", Toast.LENGTH_SHORT).show()
                                    if (error == null) nav.popBackStack(Routes.P_APPOINTMENTS, inclusive = false)
                                }
                            }
                        }
                    },
                    radius = 8.dp,
                    height = 48.dp,
                    fontSize = 15,
                    leadingIcon = R.drawable.lucide_ic_calendar_plus,
                )
            }
        }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = date ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    date = pickerState.selectedDateMillis
                    showPicker = false
                }) { Txt("Aceptar", 14, weight = FontWeight.Bold, color = Electric) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Txt("Cancelar", 14, color = Slate500) }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun FormLabel(text: String) {
    Txt(text, 13, weight = FontWeight.Bold, color = Color(0xFF111827), modifier = Modifier.padding(bottom = 10.dp))
}

@Composable
private fun Dropdown(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF4F8FF))
                .border(1.dp, FieldBorder, RoundedCornerShape(8.dp))
                .tap { open = true }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Txt(options.getOrElse(selected) { "" }, 14, color = DeepNavy, modifier = Modifier.weight(1f), maxLines = 1)
            LIcon(R.drawable.lucide_ic_chevron_down, DeepNavy, 18.dp)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, containerColor = White) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Txt(option, 14, color = DeepNavy) },
                    onClick = {
                        onSelect(index)
                        open = false
                    },
                )
            }
        }
    }
}

