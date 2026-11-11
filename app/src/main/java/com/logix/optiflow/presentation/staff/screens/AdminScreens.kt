package com.logix.optiflow.presentation.staff.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.domain.model.STORE_ZONE
import com.logix.optiflow.domain.model.report.ReportKind
import com.logix.optiflow.domain.model.searchbooking.StaffRole
import com.logix.optiflow.presentation.shared.components.Divider
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.StaffBackTopBar
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.staff.ReportsViewModel
import com.logix.optiflow.presentation.staff.SCHEDULE_DAYS
import com.logix.optiflow.presentation.staff.SLOT_MINUTES
import com.logix.optiflow.presentation.staff.ScheduleViewModel
import com.logix.optiflow.presentation.staff.StaffMembersViewModel
import com.logix.optiflow.presentation.staff.SuppliersViewModel
import com.logix.optiflow.presentation.staff.components.StaffButton
import com.logix.optiflow.presentation.staff.components.StaffChip
import com.logix.optiflow.presentation.staff.components.StaffInitials
import com.logix.optiflow.presentation.staff.components.StaffInput
import com.logix.optiflow.presentation.staff.components.StaffLabel
import com.logix.optiflow.presentation.staff.components.StaffPill
import com.logix.optiflow.presentation.staff.components.StaffScreen
import com.logix.optiflow.presentation.staff.components.StaffSectionTitle
import com.logix.optiflow.presentation.staff.components.StaffSelect
import com.logix.optiflow.presentation.staff.components.staffCard
import com.logix.optiflow.presentation.staff.components.staffViewModel
import com.logix.optiflow.presentation.staff.roleLabel
import com.logix.optiflow.presentation.staff.soles
import com.logix.optiflow.presentation.theme.ErrorRed
import com.logix.optiflow.presentation.theme.Inter
import com.logix.optiflow.presentation.theme.Lavender
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.StaffAccent
import com.logix.optiflow.presentation.theme.StaffBorder
import com.logix.optiflow.presentation.theme.StaffChip
import com.logix.optiflow.presentation.theme.StaffGreen
import com.logix.optiflow.presentation.theme.StaffInk
import com.logix.optiflow.presentation.theme.White
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ES = Locale.forLanguageTag("es-PE")
private val DAY_CHIP = DateTimeFormatter.ofPattern("EEE d", ES)
private val HOUR = DateTimeFormatter.ofPattern("HH:mm").withZone(STORE_ZONE)
private val MONTH = DateTimeFormatter.ofPattern("MMMM yyyy", ES)
private val MONTH_SHORT = DateTimeFormatter.ofPattern("MMM", ES)

@Composable
private fun Feedback(message: String?, error: String?) {
    message?.let {
        Txt(it, 12, color = StaffGreen, family = Inter)
        VSpace(12.dp)
    }
    error?.let {
        Txt(it, 12, color = ErrorRed, family = Inter)
        VSpace(12.dp)
    }
}

// ---------------------------------------------------------------- Personal

@Composable
fun StaffMembersScreen(nav: NavController) {
    val staff by staffViewModel().uiState.collectAsState()
    val vm: StaffMembersViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()
    LaunchedEffect(staff.storeId) { vm.load(staff.storeId) }

    StaffScreen(nav = nav, tab = null, showBottomBar = false, topBar = { StaffBackTopBar("Personal", onBack = { nav.popBackStack() }) }) {
        VSpace(16.dp)
        Txt("Equipo de ${staff.storeName}", 15, weight = FontWeight.Bold, color = StaffInk, family = Inter)
        VSpace(12.dp)
        if (state.showForm) {
            Column(Modifier.fillMaxWidth().staffCard().padding(16.dp)) {
                Txt("Nuevo miembro del personal", 13, weight = FontWeight.Bold, color = StaffInk, family = Inter)
                VSpace(12.dp)
                StaffInput(state.name, vm::setName, "Nombre completo")
                VSpace(10.dp)
                StaffInput(state.email, vm::setEmail, "Correo", keyboardType = KeyboardType.Email)
                VSpace(10.dp)
                StaffInput(state.password, vm::setPassword, "Contraseña temporal (mín. 8)")
                VSpace(10.dp)
                StaffSelect("Rol: ${roleLabel(state.role.name)}", null, onClick = vm::nextRole)
                VSpace(12.dp)
                Feedback(null, state.errorMessage)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StaffButton("Cancelar", { vm.showForm(false) }, background = White, contentColor = StaffInk, border = StaffBorder, modifier = Modifier.weight(1f))
                    StaffButton(if (state.isSaving) "Guardando…" else "Registrar", { vm.register() }, modifier = Modifier.weight(1f))
                }
            }
            VSpace(16.dp)
        } else {
            StaffButton("Agregar personal", { vm.showForm(true) }, R.drawable.lucide_ic_plus)
            VSpace(16.dp)
            Feedback(state.message, state.errorMessage)
        }
        if (state.isLoading) Txt("Cargando…", 12, color = Slate400, family = Inter)
        state.members.forEach { member ->
            Column(Modifier.fillMaxWidth().staffCard().padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StaffInitials(member.name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }, size = 40.dp)
                    HSpace(12.dp)
                    Column(Modifier.weight(1f)) {
                        Txt(member.name, 12, color = StaffInk, family = Inter, maxLines = 1)
                        Txt(member.email, 9, color = Slate400, family = Inter, maxLines = 1)
                    }
                    StaffPill(if (member.active) roleLabel(member.role) else "Inactivo", size = 8)
                }
                if (member.id != staff.staffId) {
                    VSpace(10.dp)
                    if (state.editingId == member.id) {
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StaffRole.entries.forEach { role ->
                                StaffChip(roleLabel(role.name), role.name == member.role) {
                                    if (role.name == member.role) vm.editRole(null) else vm.changeRole(member, role)
                                }
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Txt("Cambiar rol", 10, color = StaffAccent, family = Inter, modifier = Modifier.tap { vm.editRole(member.id) })
                            Txt(if (member.active) "Desactivar" else "Activar", 10, color = if (member.active) ErrorRed else StaffGreen, family = Inter, modifier = Modifier.tap { vm.toggleActive(member) })
                        }
                    }
                }
            }
            VSpace(10.dp)
        }
        VSpace(24.dp)
    }
}

// ---------------------------------------------------------------- Proveedores

@Composable
fun SuppliersScreen(nav: NavController) {
    val vm: SuppliersViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()

    StaffScreen(nav = nav, tab = null, showBottomBar = false, topBar = { StaffBackTopBar("Proveedores", onBack = { nav.popBackStack() }) }) {
        VSpace(16.dp)
        Column(Modifier.fillMaxWidth().staffCard().padding(16.dp)) {
            Txt("Nuevo proveedor", 13, weight = FontWeight.Bold, color = StaffInk, family = Inter)
            VSpace(12.dp)
            StaffInput(state.name, vm::setName, "Nombre o razón social")
            VSpace(10.dp)
            StaffInput(state.contact, vm::setContact, "Contacto (correo o teléfono, opcional)")
            VSpace(12.dp)
            Feedback(state.message, state.errorMessage)
            StaffButton(if (state.isSaving) "Guardando…" else "Registrar proveedor", { vm.register() })
        }
        VSpace(20.dp)
        StaffSectionTitle("Registrados · ${state.suppliers.size}")
        VSpace(12.dp)
        if (state.isLoading) Txt("Cargando…", 12, color = Slate400, family = Inter)
        if (!state.isLoading && state.suppliers.isEmpty()) Txt("Todavía no hay proveedores.", 12, color = Slate400, family = Inter)
        Column(Modifier.fillMaxWidth().staffCard().padding(horizontal = 16.dp)) {
            state.suppliers.forEachIndexed { index, supplier ->
                if (index > 0) Divider(StaffBorder)
                Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    Txt(supplier.name, 12, color = StaffInk, family = Inter)
                    Txt(supplier.contact ?: "Sin contacto", 9, color = Slate400, family = Inter)
                }
            }
        }
        VSpace(24.dp)
    }
}

// ---------------------------------------------------------------- Horarios

@Composable
fun ScheduleScreen(nav: NavController) {
    val staff by staffViewModel().uiState.collectAsState()
    val vm: ScheduleViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()
    LaunchedEffect(staff.storeId) { vm.load(staff.storeId) }
    val today = LocalDate.now(STORE_ZONE)
    val count = vm.preview(state)

    StaffScreen(nav = nav, tab = null, showBottomBar = false, topBar = { StaffBackTopBar("Horarios de atención", onBack = { nav.popBackStack() }) }) {
        VSpace(16.dp)
        Txt("Día", 13, color = StaffInk, family = Inter)
        VSpace(10.dp)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (0 until SCHEDULE_DAYS).map { today.plusDays(it) }.forEach { day ->
                StaffChip(DAY_CHIP.format(day).replace(".", ""), day == state.day) { vm.selectDay(day) }
            }
        }
        VSpace(18.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                StaffLabel("Desde")
                VSpace(8.dp)
                StaffInput(state.from, vm::setFrom, "09:00", keyboardType = KeyboardType.Number)
            }
            Column(Modifier.weight(1f)) {
                StaffLabel("Hasta")
                VSpace(8.dp)
                StaffInput(state.to, vm::setTo, "13:00", keyboardType = KeyboardType.Number)
            }
        }
        VSpace(16.dp)
        Txt("Duración de cada turno", 13, color = StaffInk, family = Inter)
        VSpace(10.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SLOT_MINUTES.forEach { m -> StaffChip("$m min", m == state.minutes) { vm.setMinutes(m) } }
        }
        VSpace(16.dp)
        Txt(if (count == null) "Revisa las horas." else "Se abrirán $count turnos.", 11, color = Slate400, family = Inter)
        VSpace(12.dp)
        Feedback(state.message, state.errorMessage)
        StaffButton(if (state.isSaving) "Abriendo…" else "Abrir turnos", { vm.submit() }, R.drawable.lucide_ic_calendar)
        VSpace(24.dp)
        StaffSectionTitle("Turnos de ese día · ${state.daySlots.size}")
        VSpace(12.dp)
        if (state.isLoading) Txt("Cargando…", 12, color = Slate400, family = Inter)
        if (!state.isLoading && state.daySlots.isEmpty()) Txt("No hay turnos abiertos ese día.", 12, color = Slate400, family = Inter)
        state.daySlots.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { slot ->
                    val free = slot.status.equals("AVAILABLE", ignoreCase = true)
                    Column(
                        Modifier.weight(1f).staffCard(radius = 12.dp, background = if (free) White else Lavender).padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Txt("${HOUR.format(slot.startDateTime)}–${HOUR.format(slot.endDateTime)}", 11, color = StaffInk, family = Inter)
                        Txt(if (free) "Libre" else "Reservado", 8, color = if (free) StaffGreen else StaffAccent, family = Inter)
                    }
                }
                repeat(3 - row.size) { Box(Modifier.weight(1f)) }
            }
            VSpace(8.dp)
        }
        VSpace(24.dp)
    }
}

// ---------------------------------------------------------------- Reportes

@Composable
fun ReportsScreen(nav: NavController) {
    val staff by staffViewModel().uiState.collectAsState()
    val vm: ReportsViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(staff.storeId) { vm.load(staff.storeId) }
    var pending by remember { mutableStateOf<String?>(null) }
    val saveFile =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            val csv = pending
            pending = null
            if (uri == null || csv == null) return@rememberLauncherForActivityResult
            val ok = runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) } != null }.getOrDefault(false)
            vm.saved(ok)
        }
    val report = state.report
    val monthLabel = MONTH.format(state.month).replaceFirstChar { it.uppercase() }

    StaffScreen(nav = nav, tab = null, showBottomBar = false, topBar = { StaffBackTopBar("Reportes", onBack = { nav.popBackStack() }) }) {
        VSpace(16.dp)
        Row(
            Modifier.fillMaxWidth().height(50.dp).staffCard(radius = 16.dp, border = Lavender).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LIcon(R.drawable.lucide_ic_chevron_left, StaffInk, 22.dp, Modifier.tap { vm.changeMonth(-1) })
            Txt(monthLabel, 12, color = StaffInk, family = Inter, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
            LIcon(R.drawable.lucide_ic_chevron_right, StaffInk, 22.dp, Modifier.tap { vm.changeMonth(1) })
        }
        VSpace(14.dp)
        if (report == null) {
            Txt(if (state.isLoading) "Cargando…" else state.errorMessage.orEmpty(), 12, color = Slate400, family = Inter)
            return@StaffScreen
        }
        val max = report.salesHistory.maxOf { it.second }.takeIf { it > 0 } ?: 1.0
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFF1E3D79)).padding(20.dp)) {
            Txt("Ventas cobradas", 9, color = White.copy(alpha = 0.8f), family = Inter)
            VSpace(14.dp)
            Txt(soles(report.sales.totalAmount), 26, color = White, family = Inter)
            VSpace(10.dp)
            Txt("Ticket promedio ${soles(report.sales.averageTicket)}", 9, color = White.copy(alpha = 0.8f), family = Inter)
            VSpace(16.dp)
            Row(Modifier.fillMaxWidth().height(96.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                report.salesHistory.forEach { (month, total) ->
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight((total / max).toFloat().coerceAtLeast(0.03f))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(if (month == state.month) White else StaffChip),
                    )
                }
            }
            VSpace(6.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                report.salesHistory.forEach { (month, _) ->
                    Txt(MONTH_SHORT.format(month).replace(".", ""), 8, color = White.copy(alpha = 0.8f), family = Inter, modifier = Modifier.weight(1f))
                }
            }
        }
        VSpace(16.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ReportStat("${report.sales.approvedQuotations}", "Cotizaciones aprobadas", "${report.sales.rejectedQuotations} rechazadas", Modifier.weight(1f))
            val production = report.production
            ReportStat(
                "${production?.ordersByStatus?.values?.sum() ?: 0}",
                "Órdenes de trabajo",
                production?.let { p -> "${p.delayedOrders} atrasadas" + (p.averageFabricationHours?.let { h -> " · ${"%.1f".format(Locale.US, h)} h prom." } ?: "") } ?: "—",
                Modifier.weight(1f),
            )
        }
        VSpace(14.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            val inventory = report.inventory
            ReportStat("${inventory?.unitsOut ?: 0}", "Unidades vendidas", "${inventory?.unitsIn ?: 0} ingresaron", Modifier.weight(1f))
            ReportStat("${inventory?.lowStockProducts ?: 0}", "Stock crítico", "monturas bajo el mínimo", Modifier.weight(1f))
        }
        VSpace(22.dp)
        StaffSectionTitle("Exportar en CSV")
        VSpace(12.dp)
        Feedback(state.message, state.errorMessage)
        listOf(ReportKind.SALES to "Ventas", ReportKind.PRODUCTION to "Producción", ReportKind.INVENTORY to "Inventario").forEach { (kind, label) ->
            StaffButton(
                if (state.exporting == kind) "Preparando…" else "Exportar $label",
                {
                    vm.export(kind) { csv ->
                        pending = csv
                        saveFile.launch("optiflow-${kind.path}-${state.month}.csv")
                    }
                },
                R.drawable.lucide_ic_file_text,
                background = White,
                contentColor = StaffInk,
                border = StaffBorder,
            )
            VSpace(10.dp)
        }
        VSpace(24.dp)
    }
}

@Composable
private fun ReportStat(value: String, title: String, subtitle: String, modifier: Modifier) {
    Column(modifier.height(120.dp).staffCard(radius = 20.dp, border = Lavender).padding(16.dp)) {
        Txt(value, 30, weight = FontWeight.ExtraBold, color = StaffInk, family = Inter, modifier = Modifier.align(Alignment.End))
        Spacer(Modifier.weight(1f))
        Txt(title, 13, weight = FontWeight.Bold, color = StaffInk, family = Inter)
        VSpace(6.dp)
        Txt(subtitle, 11, color = Slate400, family = Inter)
    }
}
