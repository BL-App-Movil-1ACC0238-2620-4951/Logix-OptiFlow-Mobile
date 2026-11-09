package com.logix.optiflow.presentation.booking.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.presentation.booking.BookingUiState
import com.logix.optiflow.presentation.booking.BookingViewModel
import com.logix.optiflow.presentation.booking.SlotOption
import com.logix.optiflow.presentation.navigation.Routes
import com.logix.optiflow.presentation.patient.components.PatientScreen
import com.logix.optiflow.presentation.patient.components.patientViewModel
import com.logix.optiflow.presentation.shared.components.Divider
import com.logix.optiflow.presentation.shared.components.ErrorBanner
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.IconTile
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.PatientDarkTopBar
import com.logix.optiflow.presentation.shared.components.PatientTab
import com.logix.optiflow.presentation.shared.components.Pill
import com.logix.optiflow.presentation.shared.components.SolidButton
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.card
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.theme.DeepNavy
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.Jakarta
import com.logix.optiflow.presentation.theme.Lavender
import com.logix.optiflow.presentation.theme.NavyBlue
import com.logix.optiflow.presentation.theme.SkyTint
import com.logix.optiflow.presentation.theme.Slate300
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.Slate500
import com.logix.optiflow.presentation.theme.Slate600
import com.logix.optiflow.presentation.theme.White
import java.time.format.DateTimeFormatter
import java.util.Locale

private val es = Locale("es", "PE")
private val Ink = Color(0xFF111827)
private val SoftPanel = Color(0xFFF4F6FD)

@Composable
fun BookingScreen(nav: NavController) {
    val vm: BookingViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()
    val patientVm = patientViewModel()

    BackHandler(enabled = state.step in 2..3) { vm.back() }

    val title =
        when (state.step) {
            1 -> "Elige una óptica"
            2 -> "Elige una fecha"
            else -> "Confirma tu reserva"
        }

    PatientScreen(
        nav = nav,
        tab = PatientTab.APPOINTMENTS,
        topBar = {
            PatientDarkTopBar(title, onBack = { if (!vm.back()) nav.popBackStack() })
        },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            VSpace(26.dp)
            Stepper(state.step)
            VSpace(26.dp)
            when (state.step) {
                1 -> StoreStep(state, vm)
                2 -> DateStep(state, vm)
                3 -> ConfirmStep(state, vm) { patientVm.refresh() }
                else ->
                    DoneStep(
                        state,
                        onAppointments = {
                            patientVm.refresh()
                            nav.navigate(Routes.P_APPOINTMENTS) { popUpTo(Routes.P_HOME) }
                        },
                        onHome = {
                            patientVm.refresh()
                            nav.navigate(Routes.P_HOME) { popUpTo(Routes.P_HOME) { inclusive = true } }
                        },
                    )
            }
            VSpace(20.dp)
        }
    }
}

@Composable
private fun Stepper(step: Int) {
    Box(Modifier.fillMaxWidth().height(28.dp), contentAlignment = Alignment.Center) {
        Divider(White, Modifier.padding(horizontal = 14.dp), thickness = 2.dp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            (1..4).forEach { index ->
                val done = index < step || step == 4
                val current = index == step && step != 4
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (done || current) Electric else White)
                        .then(if (done || current) Modifier else Modifier.border(1.dp, Slate300, CircleShape)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (done && !(step == 4 && index == 4)) {
                        LIcon(R.drawable.lucide_ic_check, White, 14.dp)
                    } else {
                        Txt(
                            "$index",
                            11,
                            weight = FontWeight.SemiBold,
                            color = if (done || current) White else Slate400,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.StoreStep(state: BookingUiState, vm: BookingViewModel) {
    Txt("Selecciona dónde quieres atenderte.", 14, color = Slate500, modifier = Modifier.padding(start = 10.dp))
    VSpace(20.dp)
    if (state.isLoading && state.stores.isEmpty()) {
        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Electric)
        }
    }
    state.stores.forEachIndexed { index, store ->
        val selected = index == state.selectedStoreIndex
        Row(
            Modifier
                .padding(horizontal = 6.dp)
                .fillMaxWidth()
                .card(
                    radius = 16.dp,
                    background = if (selected) Color(0xFFF7F8FF) else White,
                    border = if (selected) Electric else Lavender,
                    borderWidth = if (selected) 2.dp else 1.dp,
                ).tap { vm.selectStore(index) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Box(
                Modifier
                    .padding(top = 2.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(if (selected) 2.dp else 1.dp, if (selected) Electric else Slate300, CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(if (selected) Electric else Color.Transparent),
            )
            HSpace(12.dp)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Txt(store.name, 15, weight = FontWeight.Bold, color = DeepNavy)
                Txt(store.address, 12, color = Slate500)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LIcon(R.drawable.lucide_ic_map_pin, if (selected) Electric else Slate500, 12.dp)
                    HSpace(4.dp)
                    Txt(
                        "${store.distance} · ${store.availability}",
                        12,
                        weight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) Electric else Slate500,
                    )
                }
            }
        }
        VSpace(12.dp)
    }
    ErrorBanner(state.errorMessage)
    VSpace(4.dp)
    SolidButton(
        "Continuar",
        onClick = vm::continueFromStore,
        radius = 16.dp,
        elevation = 10.dp,
        enabled = state.stores.isNotEmpty(),
    )
}

@Composable
private fun ColumnScope.DateStep(state: BookingUiState, vm: BookingViewModel) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        MonthArrow(R.drawable.lucide_ic_chevron_left) { vm.shiftWeek(-1) }
        Txt(
            state.monthLabel,
            14,
            weight = FontWeight.Bold,
            color = DeepNavy,
            align = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        MonthArrow(R.drawable.lucide_ic_chevron_right) { vm.shiftWeek(1) }
    }
    VSpace(14.dp)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        state.weekDays.forEach { day ->
            val selected = day == state.selectedDate
            val enabled = state.hasSlots(day)
            Column(
                Modifier
                    .weight(1f)
                    .height(68.dp)
                    .card(
                        radius = 14.dp,
                        background = if (selected) Electric else White,
                        border = if (selected) DeepNavy else Lavender,
                        borderWidth = if (selected) 2.dp else 1.dp,
                    ).then(if (selected) Modifier.padding(2.dp).border(1.dp, White, RoundedCornerShape(12.dp)) else Modifier)
                    .tap { if (enabled) vm.selectDate(day) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                val weekday = day.format(DateTimeFormatter.ofPattern("EEE", es)).replace(".", "").uppercase(es)
                Txt(weekday, 10, weight = FontWeight.Bold, color = if (selected) White else if (enabled) Slate600 else Slate300)
                Txt("${day.dayOfMonth}", 18, weight = FontWeight.Bold, color = if (selected) White else if (enabled) DeepNavy else Slate300)
                if (selected) Box(Modifier.padding(top = 2.dp).size(4.dp).clip(CircleShape).background(White))
            }
        }
    }
    VSpace(22.dp)
    if (state.isLoading) {
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Electric)
        }
    } else {
        val morning = state.slotsForDate.filter { it.time.hour < 13 }
        val afternoon = state.slotsForDate.filter { it.time.hour >= 13 }
        if (state.slotsForDate.isEmpty()) {
            Txt("No hay horarios disponibles para este día.", 13, color = Slate500)
        }
        SlotGroup("Mañana", morning, state.selectedSlot, vm::selectSlot)
        SlotGroup("Tarde", afternoon, state.selectedSlot, vm::selectSlot)
    }
    VSpace(8.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SoftPanel)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(R.drawable.lucide_ic_chart_pie, Lavender, Electric, size = 40.dp, iconSize = 20.dp, radius = 12.dp)
        HSpace(14.dp)
        Column {
            Txt("Duración aproximada: 45 min", 13, weight = FontWeight.Bold, color = DeepNavy)
            Txt("Llega 10 minutos antes de tu cita.", 12, color = Slate500)
        }
    }
    VSpace(14.dp)
    ErrorBanner(state.errorMessage)
    VSpace(36.dp)
    SolidButton("Continuar", onClick = vm::continueFromDate, radius = 16.dp, elevation = 10.dp)
}

@Composable
private fun MonthArrow(icon: Int, onClick: () -> Unit) {
    Box(
        Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(SoftPanel).tap(onClick),
        contentAlignment = Alignment.Center,
    ) {
        LIcon(icon, DeepNavy, 18.dp)
    }
}

@Composable
private fun SlotGroup(title: String, slots: List<SlotOption>, selected: SlotOption?, onSelect: (SlotOption) -> Unit) {
    if (slots.isEmpty()) return
    Txt(title, 14, weight = FontWeight.Bold, color = DeepNavy)
    VSpace(10.dp)
    slots.chunked(4).forEach { chunk ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            chunk.forEach { slot ->
                val active = slot == selected
                Box(
                    Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(CircleShape)
                        .background(if (active) NavyBlue else White)
                        .border(1.dp, if (active) NavyBlue else Lavender, CircleShape)
                        .tap { onSelect(slot) },
                    contentAlignment = Alignment.Center,
                ) {
                    Txt(slot.label, 13, weight = FontWeight.Medium, color = if (active) White else DeepNavy)
                }
            }
            repeat(4 - chunk.size) { Box(Modifier.weight(1f)) }
        }
        VSpace(8.dp)
    }
    VSpace(10.dp)
}

@Composable
private fun ColumnScope.ConfirmStep(state: BookingUiState, vm: BookingViewModel, onBooked: () -> Unit) {
    val slot = state.selectedSlot
    val dateText =
        slot?.let {
            it.date.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", es)).replaceFirstChar { c -> c.titlecase(es) } +
                " · " + it.label
        }.orEmpty()
    Row(
        Modifier.fillMaxWidth().card(radius = 16.dp, border = Lavender).padding(16.dp),
    ) {
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(White).border(1.dp, Lavender, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            LIcon(R.drawable.lucide_ic_calendar, NavyBlue, 20.dp)
        }
        HSpace(14.dp)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Pill("Horario disponible", White, Electric, icon = R.drawable.lucide_ic_check, border = SkyTint, size = 11)
            Txt("Reserva de cita", 20, weight = FontWeight.ExtraBold, color = DeepNavy)
            Txt(dateText, 14, color = Slate600)
        }
    }
    VSpace(18.dp)
    Row(
        Modifier.fillMaxWidth().card(radius = 16.dp, border = SkyTint).padding(16.dp),
    ) {
        Box(
            Modifier.size(22.dp).clip(CircleShape).border(1.5.dp, Electric, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Txt("?", 12, weight = FontWeight.Bold, color = Electric)
        }
        HSpace(12.dp)
        Column {
            Txt("La óptica asignará el tipo de atención", 13, weight = FontWeight.Bold, color = Ink)
            Txt(
                "Reservas el horario; la óptica revisará tu necesidad y confirmará el servicio correspondiente.",
                12,
                color = Slate600,
                lineHeight = 18.sp,
            )
        }
    }
    VSpace(24.dp)
    Txt("Detalle", 17, weight = FontWeight.Bold, color = DeepNavy)
    VSpace(10.dp)
    Column(Modifier.fillMaxWidth().card(radius = 16.dp, border = Lavender).padding(horizontal = 14.dp)) {
        val store = state.selectedStore
        ConfirmRow(R.drawable.lucide_ic_map_pin, "Sucursal", store?.let { if (it.id != null) it.name else "Óptica Visión Norte · ${it.name}" } ?: "—")
        Divider(Lavender)
        ConfirmRow(R.drawable.lucide_ic_user_round, "Paciente", state.patientName)
        Divider(Lavender)
        ConfirmRow(R.drawable.lucide_ic_chart_pie, "Duración", "45 minutos")
    }
    VSpace(22.dp)
    Txt("Motivo de consulta", 14, weight = FontWeight.Bold, color = DeepNavy)
    Txt("Opcional", 12, color = Slate400)
    VSpace(10.dp)
    BasicTextField(
        value = state.reason,
        onValueChange = vm::onReasonChange,
        cursorBrush = SolidColor(Electric),
        textStyle = TextStyle(fontFamily = Jakarta, fontSize = 13.sp, color = DeepNavy),
        decorationBox = { inner ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .card(radius = 14.dp, border = Lavender)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (state.reason.isEmpty()) Txt("Cuéntanos brevemente qué necesitas...", 13, color = Slate400)
                inner()
            }
        },
    )
    VSpace(12.dp)
    ErrorBanner(state.errorMessage)
    VSpace(10.dp)
    SolidButton(
        "Reservar cita",
        onClick = { vm.confirm(onBooked) },
        radius = 16.dp,
        elevation = 10.dp,
        loading = state.isLoading,
    )
}

@Composable
private fun ConfirmRow(icon: Int, label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(icon, Color(0xFFEEF2FF), NavyBlue, size = 28.dp, iconSize = 15.dp, radius = 8.dp)
        HSpace(14.dp)
        Column {
            Txt(label, 12, color = Slate400)
            Txt(value, 15, weight = FontWeight.Bold, color = DeepNavy)
        }
    }
}

@Composable
private fun ColumnScope.DoneStep(state: BookingUiState, onAppointments: () -> Unit, onHome: () -> Unit) {
    val slot = state.selectedSlot
    VSpace(40.dp)
    Box(Modifier.align(Alignment.CenterHorizontally).size(150.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(150.dp).clip(CircleShape).border(1.dp, Lavender, CircleShape))
        Box(Modifier.size(112.dp).clip(CircleShape).background(Color(0xFFE6EBFD)).border(1.dp, Lavender, CircleShape))
        Box(
            Modifier.size(76.dp).clip(CircleShape).background(Electric),
            contentAlignment = Alignment.Center,
        ) {
            LIcon(R.drawable.lucide_ic_check, White, 30.dp)
        }
    }
    VSpace(36.dp)
    Txt(
        "RESERVA ENVIADA",
        11,
        weight = FontWeight.Bold,
        color = Electric,
        letterSpacing = 1.sp,
        modifier = Modifier.align(Alignment.CenterHorizontally),
    )
    VSpace(10.dp)
    Txt(
        "Tu horario fue reservado",
        24,
        weight = FontWeight.Bold,
        color = Ink,
        modifier = Modifier.align(Alignment.CenterHorizontally),
    )
    VSpace(10.dp)
    Txt(
        if (state.confirmed) {
            "Te esperamos en la óptica. Llega 10 minutos antes de tu cita."
        } else {
            "La óptica revisará la solicitud y te notificará el tipo de atención."
        },
        14,
        color = Slate500,
        align = TextAlign.Center,
        lineHeight = 22.sp,
        modifier = Modifier.padding(horizontal = 24.dp),
    )
    VSpace(22.dp)
    Row(
        Modifier.padding(horizontal = 20.dp).fillMaxWidth().card(radius = 16.dp, elevation = 4.dp).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(56.dp)) {
            val date = slot?.date
            Txt(date?.format(DateTimeFormatter.ofPattern("EEE", es))?.replace(".", "")?.uppercase(es) ?: "", 10, weight = FontWeight.Bold, color = Slate500)
            Txt("${date?.dayOfMonth ?: ""}", 24, weight = FontWeight.ExtraBold, color = Ink)
            Txt(date?.format(DateTimeFormatter.ofPattern("MMMM", es))?.uppercase(es) ?: "", 10, weight = FontWeight.Bold, color = Slate500)
        }
        HSpace(12.dp)
        Canvas(Modifier.width(1.dp).height(56.dp)) {
            drawLine(
                color = Lavender,
                start = Offset(0f, 0f),
                end = Offset(0f, size.height),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
            )
        }
        HSpace(14.dp)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Pill(
                if (state.confirmed) "Confirmada" else "Pendiente de confirmación",
                Color(0xFFEEF2FF),
                Electric,
                icon = if (state.confirmed) R.drawable.lucide_ic_check else R.drawable.lucide_ic_clock,
                size = 11,
                weight = FontWeight.Medium,
                padding = PaddingValues(horizontal = 10.dp, vertical = 3.dp),
            )
            Txt("${slot?.label ?: ""} · Cita en óptica", 14, weight = FontWeight.Bold, color = Ink)
            Txt(state.selectedStore?.let { if (it.id != null) it.name else "Óptica Visión Norte · ${it.name}" }.orEmpty(), 11, color = Slate500, maxLines = 1)
        }
    }
    VSpace(26.dp)
    Box(Modifier.padding(horizontal = 20.dp)) {
        SolidButton("Ver mis citas", onClick = onAppointments, radius = 14.dp, elevation = 10.dp, fontSize = 15)
    }
    VSpace(18.dp)
    Txt(
        "Volver al inicio",
        14,
        weight = FontWeight.Bold,
        color = Electric,
        modifier = Modifier.align(Alignment.CenterHorizontally).tap(onHome),
    )
}
