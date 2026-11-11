package com.logix.optiflow.presentation.staff.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.domain.model.clinical.SaleStatus
import com.logix.optiflow.presentation.shared.components.StaffTab
import com.logix.optiflow.presentation.navigation.goStaffTab
import com.logix.optiflow.presentation.shared.components.Divider
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.StaffBackTopBar
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.staff.SaleViewModel
import com.logix.optiflow.presentation.staff.components.KeyValueRow
import com.logix.optiflow.presentation.staff.components.StaffButton
import com.logix.optiflow.presentation.staff.components.StaffInitials
import com.logix.optiflow.presentation.staff.components.StaffScreen
import com.logix.optiflow.presentation.staff.components.StaffSoft
import com.logix.optiflow.presentation.staff.components.staffCard
import com.logix.optiflow.presentation.staff.components.staffViewModel
import com.logix.optiflow.presentation.staff.soles
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.Emerald50
import com.logix.optiflow.presentation.theme.Emerald700
import com.logix.optiflow.presentation.theme.ErrorBg
import com.logix.optiflow.presentation.theme.ErrorRed
import com.logix.optiflow.presentation.theme.Inter
import com.logix.optiflow.presentation.theme.Lavender
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.StaffAccent
import com.logix.optiflow.presentation.theme.StaffBorder
import com.logix.optiflow.presentation.theme.StaffInk
import com.logix.optiflow.presentation.theme.White

private fun methodLabel(raw: String?): String =
    when (raw) {
        "CASH" -> "Efectivo"
        "CARD" -> "Tarjeta"
        "YAPE" -> "Yape"
        null -> "—"
        else -> raw
    }

@Composable
fun SaleScreen(nav: NavController, patientId: String) {
    val vm: SaleViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()
    val staff by staffViewModel().uiState.collectAsState()
    val patient = staff.patients.firstOrNull { it.id == patientId }
    val sale = state.sale
    val quotation = state.quotation

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar(if (sale?.status == SaleStatus.CLOSED) "Venta confirmada" else "Venta", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(16.dp)
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(StaffSoft).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StaffInitials(patient?.initials ?: "—", size = 42.dp, fontSize = 13)
            HSpace(12.dp)
            Column(Modifier.weight(1f)) {
                Txt("Paciente", 9, color = StaffInk, family = Inter)
                Txt(patient?.name ?: "Paciente", 12, color = StaffInk, family = Inter)
            }
        }
        VSpace(20.dp)
        if (state.isLoading) {
            Txt("Cargando…", 12, color = Slate400, family = Inter)
        } else if (sale != null) {
            if (sale.status == SaleStatus.CLOSED) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(80.dp).clip(CircleShape).background(Electric), contentAlignment = Alignment.Center) {
                        LIcon(R.drawable.lucide_ic_check, White, 34.dp)
                    }
                    VSpace(12.dp)
                    Txt("Venta confirmada", 22, color = StaffInk, family = Inter)
                    VSpace(6.dp)
                    Txt("Se emitió la boleta y se generó la orden de trabajo.", 12, color = Slate400, family = Inter)
                }
                VSpace(20.dp)
                Column(Modifier.fillMaxWidth().staffCard(radius = 20.dp, border = Lavender).padding(horizontal = 16.dp, vertical = 4.dp)) {
                    KeyValueRow("Boleta", sale.receiptNumber ?: "—", 11, 11)
                    Divider(Lavender)
                    KeyValueRow("Total", soles(sale.amount ?: quotation?.total ?: 0.0), 11, 11)
                    Divider(Lavender)
                    KeyValueRow("Pago", methodLabel(sale.paymentMethod), 11, 11)
                }
                VSpace(24.dp)
                StaffButton("Volver al inicio", { nav.goStaffTab(StaffTab.HOME) })
            } else {
                Txt("Resumen", 15, weight = FontWeight.Bold, color = StaffInk, family = Inter)
                VSpace(10.dp)
                Column(Modifier.fillMaxWidth().staffCard().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    quotation?.items?.forEach { KeyValueRow(it.description, soles(it.unitPrice * it.quantity), 11, 11) }
                    quotation?.discount?.let { KeyValueRow("Descuento", "-${soles(it.amount)}", 11, 11) }
                    Divider(Lavender, thickness = 2.dp)
                    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                        Txt("Total", 16, color = StaffInk, family = Inter, modifier = Modifier.weight(1f))
                        Txt(quotation?.let { soles(it.total) } ?: "—", 16, color = StaffInk, family = Inter)
                    }
                }
                VSpace(18.dp)
                if (sale.status == SaleStatus.PAID) {
                    SaleNotice("Pagada con ${methodLabel(sale.paymentMethod).lowercase()}. Cierra la venta para emitir la boleta.", Emerald50, Emerald700)
                    VSpace(16.dp)
                    StaffButton(if (state.isSaving) "Cerrando…" else "Cerrar venta y emitir boleta", { vm.closePaidSale() })
                } else {
                    if (sale.paymentStatus == "REJECTED") {
                        SaleNotice("El último pago con tarjeta o Yape fue rechazado.", ErrorBg, ErrorRed)
                        VSpace(12.dp)
                    }
                    Txt("Pendiente de pago", 13, color = StaffAccent, family = Inter)
                    VSpace(12.dp)
                    StaffButton(
                        if (state.isSaving) "Registrando…" else "Cobrar ${quotation?.let { soles(it.total) } ?: ""} en efectivo",
                        { vm.payCashAndClose() },
                        R.drawable.lucide_ic_credit_card,
                    )
                    VSpace(12.dp)
                    Txt(
                        "Para pagar con tarjeta o Yape, el paciente entra a Mis compras en su app y toca Pagar.",
                        11,
                        color = Slate400,
                        family = Inter,
                    )
                    VSpace(12.dp)
                    StaffButton("Actualizar estado del pago", { vm.refresh() }, background = White, contentColor = StaffInk, border = StaffBorder)
                }
            }
        }
        state.errorMessage?.let {
            VSpace(12.dp)
            SaleNotice(it, ErrorBg, ErrorRed)
        }
        VSpace(28.dp)
    }
}

@Composable
private fun SaleNotice(text: String, background: Color, color: Color) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(background).padding(12.dp)) {
        Txt(text, 12, color = color, family = Inter, modifier = Modifier.weight(1f))
    }
}
