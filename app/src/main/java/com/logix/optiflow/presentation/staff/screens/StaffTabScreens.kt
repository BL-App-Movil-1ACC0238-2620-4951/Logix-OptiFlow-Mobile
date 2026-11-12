package com.logix.optiflow.presentation.staff.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.data.demo.AgendaItem
import com.logix.optiflow.data.demo.AgendaStatus
import com.logix.optiflow.data.demo.ProductionStage
import com.logix.optiflow.domain.model.STORE_ZONE
import com.logix.optiflow.presentation.navigation.Routes
import com.logix.optiflow.presentation.patient.screens.InfoTopic
import com.logix.optiflow.presentation.navigation.switchRoot
import com.logix.optiflow.presentation.search.screens.SearchField
import com.logix.optiflow.presentation.shared.components.Divider
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.StaffBell
import com.logix.optiflow.presentation.shared.components.StaffCircleAction
import com.logix.optiflow.presentation.shared.components.StaffTab
import com.logix.optiflow.presentation.shared.components.StaffTopBar
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.patient.components.image
import com.logix.optiflow.presentation.staff.InventoryRow
import com.logix.optiflow.presentation.staff.InventoryViewModel
import com.logix.optiflow.presentation.staff.StaffFeature
import com.logix.optiflow.presentation.staff.productionAlertCount
import com.logix.optiflow.presentation.staff.can
import com.logix.optiflow.presentation.staff.soles
import com.logix.optiflow.presentation.staff.components.StaffButton
import com.logix.optiflow.presentation.staff.components.StaffChip
import com.logix.optiflow.presentation.staff.components.StaffFab
import com.logix.optiflow.presentation.staff.components.StaffHero
import com.logix.optiflow.presentation.staff.components.StaffIconTile
import com.logix.optiflow.presentation.staff.components.StaffIndigoText
import com.logix.optiflow.presentation.staff.components.StaffInitials
import com.logix.optiflow.presentation.staff.components.StaffPill
import com.logix.optiflow.presentation.staff.components.StaffScreen
import com.logix.optiflow.presentation.staff.components.StaffSectionTitle
import com.logix.optiflow.presentation.staff.components.staffCard
import com.logix.optiflow.presentation.staff.components.staffViewModel
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.ErrorRed
import com.logix.optiflow.presentation.theme.Inter
import com.logix.optiflow.presentation.theme.Lavender
import com.logix.optiflow.presentation.theme.Slate300
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.StaffAccent
import com.logix.optiflow.presentation.theme.StaffBlue200
import com.logix.optiflow.presentation.theme.StaffBlue50
import com.logix.optiflow.presentation.theme.StaffBorder
import com.logix.optiflow.presentation.theme.StaffChip
import com.logix.optiflow.presentation.theme.StaffGreen
import com.logix.optiflow.presentation.theme.StaffHeader
import com.logix.optiflow.presentation.theme.StaffHero
import com.logix.optiflow.presentation.theme.StaffInk
import com.logix.optiflow.presentation.theme.StaffMuted
import com.logix.optiflow.presentation.theme.White

private val WEEK_DAYS = listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB")

private fun greeting(): String =
    when (java.time.LocalTime.now(STORE_ZONE).hour) {
        in 5..11 -> "Buenos días"
        in 12..18 -> "Buenas tardes"
        else -> "Buenas noches"
    }

@Composable
fun StaffDashboardScreen(nav: NavController) {
    val vm = staffViewModel()
    val state by vm.uiState.collectAsState()
    val role = state.role
    StaffScreen(
        nav = nav,
        tab = StaffTab.HOME,
        topBar = {
            StaffTopBar("${greeting()}, ${state.firstName}", eyebrow = state.storeName) {
                if (role.can(StaffFeature.ALERTS)) StaffBell { nav.navigate(Routes.S_ALERTS) }
            }
        },
    ) {
        VSpace(16.dp)
        StaffHero(background = StaffHero, padding = androidx.compose.foundation.layout.PaddingValues(22.dp, 24.dp)) {
            Row {
                Column(Modifier.weight(1f)) {
                    Txt(state.agendaDateLabel, 11, color = Slate300, family = Inter, letterSpacing = 1.54.sp)
                    VSpace(10.dp)
                    Txt("Tu jornada, de un vistazo", 21, weight = FontWeight.Bold, color = White, family = Inter, letterSpacing = (-0.9).sp)
                    VSpace(6.dp)
                    Txt("Todo listo para comenzar.", 13, color = Slate300, family = Inter)
                }
                Box(Modifier.size(42.dp).clip(CircleShape).background(StaffChip), contentAlignment = Alignment.Center) {
                    Txt(state.initials, 10, color = StaffHeader, family = Inter)
                }
            }
        }
        VSpace(20.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            if (role.can(StaffFeature.AGENDA)) {
                val pending = state.agenda.count { it.status != AgendaStatus.DONE }
                StatCard(R.drawable.lucide_ic_calendar, "${state.agenda.size}", "Citas de hoy", "$pending pendientes", Modifier.weight(1f)) {
                    nav.navigate(Routes.S_AGENDA)
                }
            }
            if (role.can(StaffFeature.PRODUCTION)) {
                val active = state.productionOrders.count { it.stage != ProductionStage.DELIVERED }
                val ready = state.productionOrders.count { it.stage == ProductionStage.READY_FOR_DELIVERY }
                StatCard(R.drawable.lucide_ic_briefcase, "$active", "En producción", "$ready por entregar", Modifier.weight(1f)) {
                    nav.navigate(Routes.S_PRODUCTION)
                }
            }
        }
        if (role.can(StaffFeature.AGENDA)) {
            VSpace(24.dp)
            StaffSectionTitle("Próxima atención", "Ver agenda") { nav.navigate(Routes.S_AGENDA) }
            VSpace(12.dp)
            val next = state.agenda.firstOrNull { it.status != AgendaStatus.DONE }
            if (next == null) {
                Txt("No hay más citas para hoy.", 12, color = Slate400, family = Inter)
            } else {
                Row(
                    Modifier.fillMaxWidth().staffCard().tap { nav.navigate(Routes.staffPatient(next.patientId)) }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StaffInitials(initialsOf(next.name))
                    HSpace(10.dp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        StaffPill(
                            when (next.status) {
                                AgendaStatus.WAITING -> "En sala de espera"
                                AgendaStatus.PENDING -> "Por confirmar"
                                else -> "Confirmada"
                            },
                        )
                        Txt(next.name, 14, color = StaffInk, family = Inter)
                        Txt("${next.time} · ${next.service}", 11, color = Slate400, family = Inter)
                    }
                    LIcon(R.drawable.lucide_ic_chevron_right, StaffInk, 22.dp)
                }
            }
        }
        val actions =
            listOfNotNull(
                Triple(R.drawable.lucide_ic_users, "Nuevo paciente", Routes.S_NEW_PATIENT).takeIf { role.can(StaffFeature.PATIENTS) },
                Triple(R.drawable.lucide_ic_scan, "Escanear", Routes.S_SCANNER)
                    .takeIf { role.can(StaffFeature.STOCK) || role.can(StaffFeature.INVENTORY) },
                Triple(R.drawable.lucide_ic_file_text, "Cotización", Routes.S_QUOTE).takeIf { role.can(StaffFeature.QUOTES) },
                Triple(R.drawable.lucide_ic_credit_card, "Nueva venta", Routes.S_QUOTE).takeIf { role.can(StaffFeature.SALES) },
                Triple(R.drawable.lucide_ic_briefcase, "Órdenes", Routes.S_PRODUCTION)
                    .takeIf { role.can(StaffFeature.PRODUCTION) && !role.can(StaffFeature.SALES) },
            )
        if (actions.isNotEmpty()) {
            VSpace(24.dp)
            StaffSectionTitle("Acciones frecuentes")
            VSpace(12.dp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            actions.forEach { (icon, label, route) ->
                Column(
                    Modifier.weight(1f).height(82.dp).staffCard(radius = 16.dp).tap { nav.navigate(route) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterVertically),
                ) {
                    LIcon(icon, StaffInk, 22.dp)
                    Txt(label, 10, color = StaffInk, family = Inter, maxLines = 1)
                }
            }
        }
        if (role.can(StaffFeature.ALERTS)) {
            val inventory: InventoryViewModel = hiltViewModel()
            val inventoryState by inventory.uiState.collectAsState()
            LifecycleResumeEffect(state.storeId) {
                inventory.loadAlerts(state.storeId)
                onPauseOrDispose {}
            }
            val stockAlerts = inventoryState.alerts.size
            val productionAlerts = productionAlertCount(state.productionOrders)
            val total = stockAlerts + productionAlerts
            VSpace(18.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .staffCard(radius = 16.dp, background = StaffBlue50, border = StaffBlue200)
                    .tap { nav.navigate(Routes.S_ALERTS) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StaffIconTile(R.drawable.lucide_ic_bell, size = 37.dp, background = Lavender, tint = StaffInk, radius = 12.dp)
                HSpace(11.dp)
                Column(Modifier.weight(1f)) {
                    Txt(
                        when (total) {
                            0 -> "Sin alertas por ahora"
                            1 -> "1 alerta requiere atención"
                            else -> "$total alertas requieren atención"
                        },
                        12,
                        color = StaffInk,
                        family = Inter,
                    )
                    Txt("$stockAlerts de stock · $productionAlerts de producción", 11, color = StaffMuted, family = Inter)
                }
                LIcon(R.drawable.lucide_ic_chevron_right, StaffInk, 22.dp)
            }
        }
        VSpace(32.dp)
    }
}

@Composable
private fun StatCard(icon: Int, value: String, title: String, subtitle: String, modifier: Modifier, onClick: (() -> Unit)? = null) {
    // Sin onClick la tarjeta solo muestra el dato y no responde al toque.
    Column(modifier.height(124.dp).staffCard().then(if (onClick != null) Modifier.tap(onClick) else Modifier).padding(14.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            StaffIconTile(icon)
            Spacer(Modifier.weight(1f))
            Txt(value, 30, weight = FontWeight.ExtraBold, color = StaffInk, family = Inter)
        }
        Spacer(Modifier.weight(1f))
        Txt(title, 13, weight = FontWeight.Bold, color = StaffInk, family = Inter)
        VSpace(4.dp)
        Txt(subtitle, 11, color = Slate400, family = Inter)
    }
}

private val agendaFilters =
    listOf(
        "Todas" to { _: AgendaItem -> true },
        "En espera" to { item: AgendaItem -> item.status == AgendaStatus.WAITING },
        "Confirmadas" to { item: AgendaItem -> item.status == AgendaStatus.CONFIRMED },
        "Retiros" to { item: AgendaItem -> item.status == AgendaStatus.PICKUP },
    )

@Composable
fun StaffAgendaScreen(nav: NavController) {
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val vm = staffViewModel()
    // Se recarga al entrar a la pestaña y al volver (por ejemplo, tras agendar una cita).
    LifecycleResumeEffect(Unit) {
        vm.refreshAgenda()
        onPauseOrDispose {}
    }
    val state by vm.uiState.collectAsState()
    val agenda = state.agenda
    val waiting = agenda.filter { it.status == AgendaStatus.WAITING }
    val done = agenda.count { it.status == AgendaStatus.DONE }
    val predicate = agendaFilters[filter].second
    val next = agenda.filter { it.status != AgendaStatus.WAITING && predicate(it) }

    StaffScreen(
        nav = nav,
        tab = StaffTab.AGENDA,
        topBar = {
            StaffTopBar("Citas y agenda", eyebrow = state.storeName) {
                if (state.role.can(StaffFeature.ALERTS)) StaffBell { nav.navigate(Routes.S_ALERTS) }
            }
        },
        overlay = {
            StaffFab(
                "Nueva cita",
                onClick = { nav.navigate(Routes.newAppointment()) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 22.dp),
            )
        },
    ) {
        VSpace(16.dp)
        StaffHero(background = Color(0xFF242E78), padding = androidx.compose.foundation.layout.PaddingValues(18.dp, 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Txt(state.agendaDateLabel, 9, color = Slate300, family = Inter, letterSpacing = 1.2.sp)
                    VSpace(8.dp)
                    Txt("${agenda.size} " + (if (agenda.size == 1) "paciente" else "pacientes") + if (state.isToday) " hoy" else "", 20, color = White, family = Inter)
                    VSpace(8.dp)
                    Txt("$done consultas completadas · ${waiting.size} en sala de espera", 11, color = Slate300, family = Inter)
                }
                Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(StaffChip), contentAlignment = Alignment.Center) {
                    Txt("${agenda.size}", 13, color = StaffHeader, family = Inter)
                }
            }
        }
        VSpace(14.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.agendaWeek.forEachIndexed { index, date ->
                val dow = WEEK_DAYS[index]
                val num = date.dayOfMonth.toString()
                val selected = date == state.agendaDate
                Column(
                    Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) StaffAccent else White)
                        .tap { vm.refreshAgenda(date) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Txt(dow, 8, color = if (selected) White else StaffInk, family = Inter)
                    Txt(num, 16, color = if (selected) White else StaffInk, family = Inter)
                }
            }
        }
        VSpace(14.dp)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            agendaFilters.forEachIndexed { index, (label, pred) ->
                StaffChip("$label (${agenda.count(pred)})", filter == index) { filter = index }
            }
        }
        if (filter != 0) {
            VSpace(12.dp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                LIcon(R.drawable.lucide_ic_list_filter, StaffAccent, 14.dp)
                HSpace(6.dp)
                Txt("Filtro activo: ${agendaFilters[filter].first.lowercase()}", 10, weight = FontWeight.SemiBold, color = StaffAccent, family = Inter)
            }
        }
        if (filter == 0 || filter == 1) {
            VSpace(26.dp)
            StaffSectionTitle("En sala de espera", "Sala 1")
            VSpace(12.dp)
            waiting.forEach { item ->
                Column(Modifier.fillMaxWidth().staffCard().padding(14.dp)) {
                    Row(Modifier.tap { nav.navigate(Routes.staffPatient(item.patientId)) }, verticalAlignment = Alignment.CenterVertically) {
                        StaffInitials(initialsOf(item.name))
                        HSpace(12.dp)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            StaffPill("En sala", icon = R.drawable.lucide_ic_clock)
                            Txt(item.name, 14, color = StaffInk, family = Inter)
                            Txt("${item.time} · ${item.service}", 11, color = Slate400, family = Inter)
                        }
                        LIcon(R.drawable.lucide_ic_chevron_right, StaffInk, 20.dp)
                    }
                    val appointmentId = item.appointmentId
                    if (state.role.can(StaffFeature.CLINICAL) && appointmentId != null) {
                        VSpace(14.dp)
                        StaffButton(
                            "Iniciar atención clínica",
                            onClick = { nav.navigate(Routes.clinicalCare(appointmentId, item.patientId)) },
                            icon = R.drawable.lucide_ic_eye,
                            height = 48.dp,
                        )
                    }
                }
            }
        }
        if (filter != 1) {
            VSpace(26.dp)
            StaffSectionTitle("Siguientes citas", "${next.size} por atender")
            VSpace(12.dp)
            next.forEach { item ->
                AgendaRow(item) { nav.navigate(Routes.staffPatient(item.patientId)) }
                VSpace(10.dp)
            }
        }
        VSpace(90.dp)
    }
}

private fun initialsOf(name: String): String =
    name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

@Composable
private fun AgendaRow(item: AgendaItem, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().staffCard().tap(onClick).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(
            Modifier.size(width = 66.dp, height = 60.dp).clip(RoundedCornerShape(12.dp)).background(StaffChip),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Txt(item.time, 15, color = StaffInk, family = Inter)
            Txt(item.duration, 9, color = StaffMuted, family = Inter)
        }
        HSpace(12.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            when (item.status) {
                AgendaStatus.CONFIRMED -> StaffPill("Confirmada")
                AgendaStatus.PENDING -> StaffPill("Por confirmar", icon = R.drawable.lucide_ic_clock)
                AgendaStatus.PICKUP -> StaffPill("Retiro", icon = R.drawable.lucide_ic_package)
                else -> StaffPill("Atendida")
            }
            Txt(item.name, 14, color = StaffInk, family = Inter)
            Txt(item.service, 11, color = Slate400, family = Inter)
        }
        LIcon(R.drawable.lucide_ic_chevron_right, StaffInk, 20.dp)
    }
}

@Composable
fun StaffPatientsScreen(nav: NavController) {
    val vm = staffViewModel()
    val state by vm.uiState.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val live = state.patientsFromApi
    // "En atención" son los pacientes de la sala de espera de hoy y "En taller" los que tienen una orden sin entregar.
    val waitingIds =
        if (state.agendaFromApi) state.agenda.filter { it.status == AgendaStatus.WAITING }.map { it.patientId }.toSet() else emptySet()
    val patients =
        state.patients.filter {
            !live || filter == 0 || (filter == 1 && it.id in waitingIds) || (filter == 2 && it.id in state.workshopPatientIds)
        }.filter {
            query.isBlank() || it.fullName.contains(query, true) || it.rut.contains(query) || it.phone.contains(query) ||
                it.email.contains(query, true)
        }

    StaffScreen(
        nav = nav,
        tab = StaffTab.PATIENTS,
        topBar = {
            StaffTopBar("Pacientes") { StaffCircleAction(R.drawable.lucide_ic_plus) { nav.navigate(Routes.S_NEW_PATIENT) } }
        },
        overlay = {
            StaffFab(
                "Nuevo paciente",
                onClick = { nav.navigate(Routes.S_NEW_PATIENT) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 22.dp),
            )
        },
    ) {
        VSpace(16.dp)
        StaffHero(padding = androidx.compose.foundation.layout.PaddingValues(20.dp, 22.dp)) {
            Txt("Expedientes de pacientes", 18, color = White, family = Inter)
            VSpace(8.dp)
            Txt("Fichas optométricas, recetas y seguimientos al día.", 11, color = Slate300, family = Inter)
            VSpace(22.dp)
            Row {
                StatColumn(state.patients.size.toString(), "Total pacientes", White, Modifier.weight(1f))
                StatColumn(state.newPatientsThisMonth.toString(), "Nuevos este mes", Color(0xFF34D399), Modifier.weight(1f))
                StatColumn(state.patientsWithRecords.toString(), "Con ficha vigente", White, Modifier.weight(1f))
            }
        }
        VSpace(16.dp)
        SearchField(query, { query = it }, if (live) "Buscar por nombre, correo o teléfono" else "Buscar por nombre, RUT o teléfono")
        VSpace(12.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (
                if (live) {
                    listOf(
                        "Todos (${state.patients.size})",
                        "En atención (${state.patients.count { it.id in waitingIds }})",
                        "En taller (${state.patients.count { it.id in state.workshopPatientIds }})",
                    )
                } else {
                    listOf("Todos (1.248)", "En atención (1)", "En taller (12)")
                }
            ).forEachIndexed { index, label ->
                StaffChip(label, filter == index) { filter = index }
            }
        }
        VSpace(26.dp)
        StaffSectionTitle("Pacientes recientes", "Ordenar por fecha")
        VSpace(12.dp)
        val tints = listOf(Lavender, Color(0xFFDBEAFE), Color(0xFFF3F4FF), Color(0xFFE0E7FF))
        patients.forEachIndexed { index, patient ->
            Row(
                Modifier.fillMaxWidth().staffCard().tap { nav.navigate(Routes.staffPatient(patient.id)) }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StaffInitials(patient.initials, size = 52.dp, background = tints[index % tints.size], fontSize = 14)
                HSpace(16.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Txt(patient.fullName, 14, color = StaffInk, family = Inter)
                    Txt(patient.email.ifBlank { patient.rut }, 11, color = Slate400, family = Inter)
                    Txt(patient.note, 11, color = Slate400, family = Inter)
                }
                LIcon(R.drawable.lucide_ic_chevron_right, StaffInk, 20.dp)
            }
            VSpace(12.dp)
        }
        VSpace(80.dp)
    }
}

@Composable
private fun StatColumn(value: String, label: String, color: Color, modifier: Modifier) {
    Column(modifier) {
        Txt(value, 18, color = color, family = Inter)
        VSpace(4.dp)
        Txt(label, 9, color = Slate300, family = Inter)
    }
}

@Composable
fun StaffInventoryScreen(nav: NavController) {
    val staff by staffViewModel().uiState.collectAsState()
    val vm: InventoryViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()
    val role = staff.role
    // Se recarga al volver de una reposición o de una montura nueva.
    LifecycleResumeEffect(staff.storeId) {
        vm.load(staff.storeId)
        onPauseOrDispose {}
    }
    val rows = state.filtered

    StaffScreen(
        nav = nav,
        tab = StaffTab.INVENTORY,
        topBar = { StaffTopBar("Inventario") { StaffCircleAction(R.drawable.lucide_ic_scan) { nav.navigate(Routes.S_SCANNER) } } },
    ) {
        VSpace(16.dp)
        StaffHero(radius = 20.dp, padding = androidx.compose.foundation.layout.PaddingValues(18.dp, 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Txt("CONTROL DE STOCK · ${staff.storeName.uppercase()}", 8, color = Slate300, family = Inter, letterSpacing = 1.sp, maxLines = 1)
                    VSpace(8.dp)
                    Txt("Catálogo y stock", 18, color = White, family = Inter)
                    VSpace(6.dp)
                    Txt("${state.rows.size} modelos en el catálogo de la cadena.", 10, color = Slate300, family = Inter)
                }
                LIcon(R.drawable.lucide_ic_layout_grid, Color(0xFFB7C4FF), 44.dp)
            }
        }
        VSpace(12.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            StatCard(R.drawable.lucide_ic_eye, "${state.totalUnits}", "En stock", "${state.lowCount} por reponer", Modifier.weight(1f)) {
                if (role.can(StaffFeature.ALERTS)) nav.navigate(Routes.S_ALERTS)
            }
            if (role.can(StaffFeature.PRODUCTION)) {
                val open = staff.productionOrders.filter { it.stage != ProductionStage.DELIVERED }
                StatCard(R.drawable.lucide_ic_briefcase, "${open.size}", "En taller / lab", "${open.count { it.needsAttention }} con retraso", Modifier.weight(1f)) {
                    nav.navigate(Routes.S_PRODUCTION)
                }
            } else {
                StatCard(R.drawable.lucide_ic_briefcase, "${state.rows.count { (it.stock?.quantity ?: 0) == 0 }}", "Sin stock", "modelos agotados aquí", Modifier.weight(1f))
            }
        }
        VSpace(12.dp)
        SearchField(state.query, vm::setQuery, "Buscar modelo, SKU o color")
        VSpace(26.dp)
        StaffSectionTitle("Catálogo · ${rows.size} ${if (rows.size == 1) "modelo" else "modelos"}")
        VSpace(12.dp)
        when {
            state.isLoading -> Txt("Cargando…", 12, color = Slate400, family = Inter)
            state.errorMessage != null -> Txt(state.errorMessage.orEmpty(), 12, color = Slate400, family = Inter)
            rows.isEmpty() -> Txt("No hay monturas que coincidan.", 12, color = Slate400, family = Inter)
        }
        rows.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                row.forEach { item ->
                    ProductCard(item, Modifier.weight(1f)) { nav.navigate(Routes.product(item.frame.sku)) }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            VSpace(14.dp)
        }
        val actions =
            listOfNotNull(
                Triple(R.drawable.lucide_ic_plus, "Nueva montura", Routes.S_NEW_FRAME).takeIf { role.can(StaffFeature.CATALOG) },
                Triple(R.drawable.lucide_ic_box, "Ingresar stock", Routes.movement()).takeIf { role.can(StaffFeature.INVENTORY) },
                Triple(R.drawable.lucide_ic_bell, "Alertas", Routes.S_ALERTS).takeIf { role.can(StaffFeature.ALERTS) },
                Triple(R.drawable.lucide_ic_file_text, "Órdenes", Routes.S_PRODUCTION).takeIf { role.can(StaffFeature.PRODUCTION) },
            )
        if (actions.isNotEmpty()) {
            VSpace(14.dp)
            StaffSectionTitle("Acciones rápidas")
            VSpace(12.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                actions.forEach { (icon, label, route) ->
                    Column(
                        Modifier.weight(1f).height(82.dp).staffCard(radius = 16.dp).tap { nav.navigate(route) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterVertically),
                    ) {
                        LIcon(icon, StaffInk, 22.dp)
                        Txt(label, 10, color = StaffInk, family = Inter, maxLines = 1)
                    }
                }
            }
        }
        VSpace(28.dp)
    }
}

/** Etiqueta del stock de una montura en la óptica. */
fun stockTag(row: InventoryRow): String {
    val quantity = row.stock?.quantity ?: 0
    return when {
        quantity == 0 -> "Sin stock"
        row.stock?.low == true -> "Stock bajo"
        else -> "Disponible"
    }
}

@Composable
private fun ProductCard(row: InventoryRow, modifier: Modifier, onClick: () -> Unit) {
    val frame = row.frame
    val quantity = row.stock?.quantity ?: 0
    Column(modifier.staffCard().tap(onClick).padding(12.dp)) {
        Image(
            painter = painterResource(frame.image),
            contentDescription = frame.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(92.dp).clip(RoundedCornerShape(12.dp)),
        )
        VSpace(10.dp)
        StaffPill(stockTag(row), icon = if (row.stock?.low == true || quantity == 0) R.drawable.lucide_ic_bell else R.drawable.lucide_ic_check)
        VSpace(8.dp)
        Txt(frame.name, 13, color = StaffInk, family = Inter, maxLines = 1)
        VSpace(4.dp)
        Txt(listOfNotNull(frame.color, frame.material).joinToString(" · ").ifBlank { frame.sku }, 9, color = Slate400, family = Inter, maxLines = 1)
        VSpace(14.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Txt(soles(frame.price), 13, weight = FontWeight.SemiBold, color = StaffInk, family = Inter, modifier = Modifier.weight(1f))
            Txt("$quantity disp.", 9, color = if (quantity == 0) ErrorRed else StaffGreen, family = Inter)
        }
        VSpace(14.dp)
    }
}

@Composable
fun StaffMoreScreen(nav: NavController) {
    val vm = staffViewModel()
    val state by vm.uiState.collectAsState()
    val role = state.role
    StaffScreen(nav = nav, tab = StaffTab.MORE, topBar = { StaffTopBar("Más herramientas") }) {
        val operation =
            listOfNotNull(
                ToolItem(R.drawable.lucide_ic_credit_card, "Ventas y pagos", "Nueva venta desde una cotización") { nav.navigate(Routes.S_QUOTE) }
                    .takeIf { role.can(StaffFeature.SALES) },
                ToolItem(R.drawable.lucide_ic_file_text, "Cotizaciones", "Pendientes y enviadas") { nav.navigate(Routes.S_QUOTE) }
                    .takeIf { role.can(StaffFeature.QUOTES) },
                ToolItem(
                    R.drawable.lucide_ic_briefcase,
                    "Órdenes de trabajo",
                    "Producción y entregas",
                    badge = state.productionOrders.count { it.stage != ProductionStage.DELIVERED }.takeIf { it > 0 }?.toString(),
                ) {
                    nav.navigate(Routes.S_PRODUCTION)
                }.takeIf { role.can(StaffFeature.PRODUCTION) },
            )
        if (operation.isNotEmpty()) {
            VSpace(30.dp)
            Txt("Operación", 18, weight = FontWeight.Bold, color = StaffInk, family = Inter)
            VSpace(12.dp)
            ToolList(operation)
        }
        val management =
            listOfNotNull(
                ToolItem(R.drawable.lucide_ic_bell, "Alertas", "Stock crítico y órdenes") { nav.navigate(Routes.S_ALERTS) }
                    .takeIf { role.can(StaffFeature.ALERTS) },
                ToolItem(R.drawable.lucide_ic_chart_no_axes_column, "Reportes", "Ventas, producción e inventario del mes") { nav.navigate(Routes.S_REPORTS) }
                    .takeIf { role.can(StaffFeature.REPORTS) },
                ToolItem(R.drawable.lucide_ic_user, "Personal", "Altas, roles y accesos") { nav.navigate(Routes.S_STAFF_MEMBERS) }
                    .takeIf { role.can(StaffFeature.MANAGEMENT) },
                ToolItem(R.drawable.lucide_ic_box, "Proveedores", null) { nav.navigate(Routes.S_SUPPLIERS) }
                    .takeIf { role.can(StaffFeature.MANAGEMENT) },
                ToolItem(R.drawable.lucide_ic_calendar, "Horarios de atención", "Abrir turnos para reservar") { nav.navigate(Routes.S_SCHEDULE) }
                    .takeIf { role.can(StaffFeature.MANAGEMENT) },
                ToolItem(R.drawable.lucide_ic_settings, "Configuración y permisos", null) { nav.navigate(Routes.P_SETTINGS + "?staff=true") },
                ToolItem(R.drawable.lucide_ic_circle_question_mark, "Ayuda y documentación", null) {
                    nav.navigate(Routes.info(InfoTopic.HELP.name, staff = true))
                },
                ToolItem(R.drawable.lucide_ic_user, "Cerrar sesión", null) { vm.clearSession { nav.switchRoot(Routes.AUTH) } },
            )
        VSpace(26.dp)
        Txt("Gestión", 18, weight = FontWeight.Bold, color = StaffInk, family = Inter)
        VSpace(12.dp)
        ToolList(management)
        VSpace(28.dp)
    }
}

private class ToolItem(val icon: Int, val title: String, val subtitle: String?, val badge: String? = null, val onClick: () -> Unit)

@Composable
private fun ToolList(items: List<ToolItem>) {
    ToolGroup {
        items.forEachIndexed { index, item ->
            if (index > 0) Divider(StaffBorder)
            ToolRow(item.icon, item.title, item.subtitle, item.badge, item.onClick)
        }
    }
}

@Composable
private fun ToolGroup(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().staffCard(radius = 20.dp).padding(horizontal = 16.dp), content = content)
}

@Composable
private fun ToolRow(icon: Int, title: String, subtitle: String?, badge: String? = null, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(64.dp).tap(onClick), verticalAlignment = Alignment.CenterVertically) {
        StaffIconTile(icon, size = 38.dp, background = Color(0xFFEEF2FF), radius = 12.dp)
        HSpace(14.dp)
        Column(Modifier.weight(1f)) {
            Txt(title, 12, color = StaffInk, family = Inter)
            if (subtitle != null) Txt(subtitle, 9, color = StaffIndigoText, family = Inter)
        }
        if (badge != null) {
            Box(Modifier.size(22.dp).clip(RoundedCornerShape(6.dp)).background(Electric), contentAlignment = Alignment.Center) {
                Txt(badge, 10, color = White, family = Inter)
            }
            HSpace(12.dp)
        }
        LIcon(R.drawable.lucide_ic_chevron_right, StaffInk, 20.dp)
    }
}

