package com.logix.optiflow.presentation.staff.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.domain.model.STORE_ZONE
import com.logix.optiflow.domain.model.production.WorkOrder
import com.logix.optiflow.domain.model.production.WorkOrderStatus
import com.logix.optiflow.presentation.shared.components.Divider
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.StaffBackTopBar
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.staff.StaffFeature
import com.logix.optiflow.presentation.staff.WorkOrderUiState
import com.logix.optiflow.presentation.staff.WorkOrderViewModel
import com.logix.optiflow.presentation.staff.can
import com.logix.optiflow.presentation.staff.components.StaffButton
import com.logix.optiflow.presentation.staff.components.StaffChip
import com.logix.optiflow.presentation.staff.components.StaffInput
import com.logix.optiflow.presentation.staff.components.StaffLabel
import com.logix.optiflow.presentation.staff.components.StaffPill
import com.logix.optiflow.presentation.staff.components.StaffScreen
import com.logix.optiflow.presentation.staff.components.staffCard
import com.logix.optiflow.presentation.staff.components.staffViewModel
import com.logix.optiflow.presentation.staff.workOrderDate
import com.logix.optiflow.presentation.staff.workOrderStatusLabel
import com.logix.optiflow.presentation.theme.Emerald50
import com.logix.optiflow.presentation.theme.Emerald700
import com.logix.optiflow.presentation.theme.ErrorBg
import com.logix.optiflow.presentation.theme.ErrorRed
import com.logix.optiflow.presentation.theme.Inter
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.StaffAccent
import com.logix.optiflow.presentation.theme.StaffBorder
import com.logix.optiflow.presentation.theme.StaffInk
import com.logix.optiflow.presentation.theme.White
import java.time.format.DateTimeFormatter
import java.util.Locale

private val HISTORY_TIME: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM · HH:mm", Locale.forLanguageTag("es-PE")).withZone(STORE_ZONE)

fun workOrderCode(order: WorkOrder): String = "OT-" + order.id.toString().take(4).uppercase(Locale.US)

@Composable
fun WorkOrderScreen(nav: NavController) {
    val vm: WorkOrderViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()
    val staff by staffViewModel().uiState.collectAsState()
    val canEdit = staff.role.can(StaffFeature.PRODUCTION)
    val order = state.order
    // El técnico no carga el directorio de pacientes: el nombre sale de las órdenes del taller.
    val patientName =
        order?.patientId?.let { id -> staff.patients.firstOrNull { it.id == id.toString() }?.name }
            ?: order?.let { o -> staff.productionOrders.firstOrNull { it.id == o.id.toString() }?.patient }

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Orden de trabajo", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(16.dp)
        when {
            state.isLoading -> Txt("Cargando…", 12, color = Slate400, family = Inter)
            order == null -> Unit
            else -> OrderContent(order, patientName, canEdit, state, vm)
        }
        state.message?.let {
            VSpace(12.dp)
            OrderNotice(it, Emerald50, Emerald700)
        }
        state.errorMessage?.let {
            VSpace(12.dp)
            OrderNotice(it, ErrorBg, ErrorRed)
        }
        VSpace(28.dp)
    }
}

@Composable
private fun OrderContent(order: WorkOrder, patientName: String?, canEdit: Boolean, state: WorkOrderUiState, vm: WorkOrderViewModel) {
    val finished = order.status == WorkOrderStatus.READY_FOR_DELIVERY || order.status == WorkOrderStatus.DELIVERED
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFDCE9FF), Color(0xFFF1F5FF))))
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StaffPill(workOrderStatusLabel(order.status), background = White)
            if (order.delayed && order.status != WorkOrderStatus.DELIVERED) {
                HSpace(8.dp)
                StaffPill("Atrasada", icon = R.drawable.lucide_ic_bell, background = White)
            }
        }
        VSpace(14.dp)
        Txt("${workOrderCode(order)} · ${patientName ?: "Paciente"}", 20, color = StaffInk, family = Inter)
        VSpace(6.dp)
        Txt(
            when {
                order.status == WorkOrderStatus.DELIVERED -> "Entregada"
                else -> "Entrega estimada: ${workOrderDate(order.estimatedDeliveryDate) ?: "sin fecha"}"
            },
            11,
            color = StaffAccent,
            family = Inter,
        )
        order.deliveryDelay?.reason?.let {
            VSpace(4.dp)
            Txt("Retraso: $it", 11, color = ErrorRed, family = Inter)
        }
    }

    VSpace(22.dp)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Txt("Lentes", 15, weight = FontWeight.Bold, color = StaffInk, family = Inter, modifier = Modifier.weight(1f))
        val canMark = canEdit && order.status == WorkOrderStatus.IN_WORKSHOP && order.lenses.any { !it.completed }
        if (canMark) Txt("Marcar todos", 12, color = StaffAccent, family = Inter, modifier = Modifier.tap { vm.completeAllLenses() })
    }
    VSpace(10.dp)
    Column(Modifier.fillMaxWidth().staffCard().padding(horizontal = 14.dp, vertical = 6.dp)) {
        order.lenses.forEachIndexed { index, lens ->
            if (index > 0) Divider(StaffBorder)
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                LIcon(if (lens.completed) R.drawable.lucide_ic_check else R.drawable.lucide_ic_clock, if (lens.completed) Emerald700 else StaffAccent, 18.dp)
                HSpace(10.dp)
                Column(Modifier.weight(1f)) {
                    Txt(lens.specifications ?: "Lente", 11, color = StaffInk, family = Inter)
                    Txt(if (lens.completed) "Fabricado" else "Por fabricar", 9, color = Slate400, family = Inter)
                }
                val id = lens.id
                if (canEdit && !lens.completed && order.status == WorkOrderStatus.IN_WORKSHOP && id != null) {
                    Txt("Marcar", 12, color = StaffAccent, family = Inter, modifier = Modifier.tap { vm.completeLens(id) })
                }
            }
        }
    }
    if (canEdit && order.status == WorkOrderStatus.PENDING) {
        VSpace(6.dp)
        Txt("Los lentes se marcan cuando la orden está en el taller.", 10, color = Slate400, family = Inter)
    }

    VSpace(20.dp)
    Txt("Técnico", 15, weight = FontWeight.Bold, color = StaffInk, family = Inter)
    VSpace(6.dp)
    Txt(order.technician?.name ?: "Sin asignar", 12, color = StaffInk, family = Inter)
    if (canEdit && !finished && state.technicians.isNotEmpty()) {
        VSpace(8.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.technicians.forEach { tech ->
                StaffChip(tech.name, tech.id == order.technician?.id) { if (tech.id != order.technician?.id) vm.assign(tech.id) }
            }
        }
    }

    VSpace(20.dp)
    Txt("Laboratorio", 15, weight = FontWeight.Bold, color = StaffInk, family = Inter)
    VSpace(6.dp)
    Txt(order.laboratory?.name ?: "Taller de la óptica", 12, color = StaffInk, family = Inter)
    val canSend = canEdit && (order.status == WorkOrderStatus.PENDING || order.status == WorkOrderStatus.IN_WORKSHOP)
    if (canSend && state.laboratories.isNotEmpty()) {
        VSpace(8.dp)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.laboratories.forEach { lab ->
                StaffChip(lab.name, lab.id == order.laboratory?.id) { if (lab.id != order.laboratory?.id) vm.sendTo(lab.id) }
            }
        }
    }

    VSpace(20.dp)
    Txt("Historial", 15, weight = FontWeight.Bold, color = StaffInk, family = Inter)
    VSpace(8.dp)
    Column(Modifier.fillMaxWidth().staffCard().padding(horizontal = 14.dp, vertical = 8.dp)) {
        order.statusHistory.forEach { change ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Txt(workOrderStatusLabel(change.status), 11, color = StaffInk, family = Inter, modifier = Modifier.weight(1f))
                Txt(change.changedAt?.let { HISTORY_TIME.format(it).replace(".", "") } ?: "—", 11, color = Slate400, family = Inter)
            }
        }
    }

    if (!canEdit || order.status == WorkOrderStatus.DELIVERED) return
    VSpace(24.dp)
    val next = WorkOrderStatus.next(order.status)
    if (next != null) {
        val label =
            when {
                state.isSaving -> "Guardando…"
                next == WorkOrderStatus.DELIVERED -> "Registrar entrega al paciente"
                else -> "Pasar a ${workOrderStatusLabel(next).lowercase()}"
            }
        StaffButton(label, { vm.advance() }, R.drawable.lucide_ic_chevron_right)
    }
    VSpace(12.dp)
    if (state.showDelay) {
        StaffLabel("Motivo del retraso")
        VSpace(6.dp)
        StaffInput(state.delayReason, vm::setDelayReason, "Ej. El laboratorio no tiene el material")
        VSpace(10.dp)
        StaffLabel("Nueva fecha de entrega")
        VSpace(6.dp)
        StaffInput(state.delayDate, vm::setDelayDate, "dd/mm/aaaa", keyboardType = KeyboardType.Number)
        VSpace(10.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StaffButton("Cancelar", { vm.showDelay(false) }, background = White, contentColor = StaffInk, border = StaffBorder, modifier = Modifier.weight(1f))
            StaffButton("Reportar", { vm.reportDelay() }, background = ErrorRed, modifier = Modifier.weight(1f))
        }
    } else {
        StaffButton("Reportar retraso", { vm.showDelay(true) }, background = White, contentColor = ErrorRed, border = StaffBorder)
    }
}

@Composable
private fun OrderNotice(text: String, background: Color, color: Color) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(background).padding(12.dp)) {
        Txt(text, 12, color = color, family = Inter, modifier = Modifier.weight(1f))
    }
}
