package com.logix.optiflow.presentation.staff.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.domain.model.clinical.Quotation
import com.logix.optiflow.domain.model.clinical.QuotationStatus
import com.logix.optiflow.presentation.navigation.Routes
import com.logix.optiflow.presentation.search.screens.SearchField
import com.logix.optiflow.presentation.shared.components.Divider
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.StaffBackTopBar
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.staff.LENS_OPTIONS
import com.logix.optiflow.presentation.staff.QuoteUiState
import com.logix.optiflow.presentation.staff.QuoteViewModel
import com.logix.optiflow.presentation.staff.TREATMENT_OPTIONS
import com.logix.optiflow.presentation.staff.components.KeyValueRow
import com.logix.optiflow.presentation.staff.components.StaffButton
import com.logix.optiflow.presentation.staff.components.StaffChip
import com.logix.optiflow.presentation.staff.components.StaffInitials
import com.logix.optiflow.presentation.staff.components.StaffInput
import com.logix.optiflow.presentation.staff.components.StaffLabel
import com.logix.optiflow.presentation.staff.components.StaffScreen
import com.logix.optiflow.presentation.staff.components.StaffSoft
import com.logix.optiflow.presentation.staff.components.staffCard
import com.logix.optiflow.presentation.staff.components.staffViewModel
import com.logix.optiflow.presentation.staff.soles
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

/** Paso previo de la cotización: el asesor busca al paciente en el directorio. */
@Composable
fun QuotePatientPickerScreen(nav: NavController) {
    val state by staffViewModel().uiState.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    val patients =
        state.patients.filter {
            query.isBlank() || it.fullName.contains(query, true) || it.phone.contains(query) || it.email.contains(query, true)
        }
    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Nueva cotización", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(16.dp)
        Txt("¿Para qué paciente es la cotización?", 15, weight = FontWeight.Bold, color = StaffInk, family = Inter)
        VSpace(4.dp)
        Txt("Se usa su última receta.", 11, color = Slate400, family = Inter)
        VSpace(14.dp)
        SearchField(query, { query = it }, "Buscar por nombre, correo o teléfono")
        VSpace(14.dp)
        if (patients.isEmpty()) {
            Txt("No hay pacientes que coincidan.", 12, color = Slate400, family = Inter)
        }
        patients.forEach { patient ->
            Row(
                Modifier.fillMaxWidth().staffCard().tap { nav.navigate(Routes.quote(patient.id)) }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StaffInitials(patient.initials, size = 44.dp, fontSize = 13)
                HSpace(14.dp)
                Column(Modifier.weight(1f)) {
                    Txt(patient.name, 14, color = StaffInk, family = Inter)
                    Txt(patient.email.ifBlank { patient.phone }, 11, color = Slate400, family = Inter)
                }
                LIcon(R.drawable.lucide_ic_chevron_right, StaffInk, 20.dp)
            }
            VSpace(10.dp)
        }
        VSpace(24.dp)
    }
}

@Composable
fun QuoteScreen(nav: NavController, patientId: String) {
    val vm: QuoteViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()
    val staff by staffViewModel().uiState.collectAsState()
    val patient = staff.patients.firstOrNull { it.id == patientId }
    val quotation = state.quotation
    LaunchedEffect(staff.storeId) { vm.loadStock(staff.storeId) }
    LaunchedEffect(state.openSaleId) {
        state.openSaleId?.let {
            vm.saleOpened()
            nav.navigate(Routes.sale(it.toString(), patientId))
        }
    }

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar(if (quotation == null) "Nueva cotización" else "Cotización", onBack = { nav.popBackStack() }) },
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
                if (state.prescriptionLabel.isNotBlank()) Txt(state.prescriptionLabel, 9, color = StaffAccent, family = Inter)
            }
        }
        VSpace(20.dp)
        when {
            state.isLoading -> Txt("Cargando…", 12, color = Slate400, family = Inter)
            state.blockedMessage != null -> {
                Notice(state.blockedMessage!!, ErrorBg, ErrorRed)
                VSpace(16.dp)
                StaffButton("Ver ficha del paciente", { nav.navigate(Routes.staffPatient(patientId)) })
            }
            quotation == null -> QuoteBuilder(state, vm)
            else -> QuoteDetail(nav, state, quotation, vm)
        }
        state.errorMessage?.let {
            VSpace(12.dp)
            Notice(it, ErrorBg, ErrorRed)
        }
        VSpace(28.dp)
    }
}

@Composable
private fun QuoteBuilder(state: QuoteUiState, vm: QuoteViewModel) {
    state.lastRejectedReason?.let {
        Notice("La cotización anterior fue rechazada: $it", ErrorBg, ErrorRed)
        VSpace(16.dp)
    }
    Txt("Montura", 15, weight = FontWeight.Bold, color = StaffInk, family = Inter)
    VSpace(10.dp)
    if (state.frames.isEmpty()) {
        Txt("No se pudo cargar el catálogo de monturas.", 11, color = Slate400, family = Inter)
    }
    state.frames.forEach { frame ->
        val selected = frame.sku == state.frameSku
        val units = state.stock[frame.sku]
        Row(
            Modifier.fillMaxWidth()
                .staffCard(border = if (selected) StaffAccent else StaffBorder)
                .tap { vm.selectFrame(frame.sku) }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Txt(frame.name, 13, color = StaffInk, family = Inter)
                Txt(listOfNotNull(frame.material, frame.color, frame.sku).joinToString(" · "), 10, color = Slate400, family = Inter)
                units?.let {
                    Txt(if (it == 0) "Sin stock en esta óptica" else "$it en esta óptica", 10, color = if (it == 0) ErrorRed else Emerald700, family = Inter)
                }
            }
            Txt(soles(frame.price), 12, color = if (selected) StaffAccent else StaffInk, family = Inter)
            if (selected) {
                HSpace(8.dp)
                LIcon(R.drawable.lucide_ic_check, StaffAccent, 18.dp)
            }
        }
        VSpace(8.dp)
    }
    VSpace(16.dp)
    Txt("Lentes", 15, weight = FontWeight.Bold, color = StaffInk, family = Inter)
    VSpace(10.dp)
    ChipRows(LENS_OPTIONS.map { it.name }, { LENS_OPTIONS.indexOfFirst { o -> o.name == it } == state.lensIndex }) {
        vm.selectLens(LENS_OPTIONS.indexOfFirst { o -> o.name == it })
    }
    if (state.lensIndex != null) {
        VSpace(10.dp)
        StaffLabel("Precio de los lentes (S/)")
        VSpace(6.dp)
        StaffInput(state.lensPrice, vm::setLensPrice, "0.00", keyboardType = KeyboardType.Decimal)
    }
    VSpace(16.dp)
    Txt("Tratamientos", 15, weight = FontWeight.Bold, color = StaffInk, family = Inter)
    VSpace(10.dp)
    ChipRows(TREATMENT_OPTIONS.map { "${it.name} · ${soles(it.price)}" }, { label -> state.treatments.any { label.startsWith("$it ·") } }) { label ->
        vm.toggleTreatment(label.substringBefore(" ·"))
    }
    VSpace(18.dp)
    val items = state.draftItems
    Column(Modifier.fillMaxWidth().staffCard().padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (items.isEmpty()) KeyValueRow("Sin productos", "—", 11, 11)
        items.forEach { KeyValueRow(it.description, soles(it.unitPrice * it.quantity), 11, 11) }
        Divider(Lavender, thickness = 2.dp)
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            Txt("Subtotal", 15, color = StaffInk, family = Inter, modifier = Modifier.weight(1f))
            Txt(soles(items.sumOf { it.unitPrice * it.quantity }), 15, color = StaffInk, family = Inter)
        }
    }
    VSpace(20.dp)
    StaffButton(if (state.isSaving) "Generando…" else "Generar cotización", { vm.generate() })
}

@Composable
private fun QuoteDetail(nav: NavController, state: QuoteUiState, quotation: Quotation, vm: QuoteViewModel) {
    val draft = quotation.status == QuotationStatus.DRAFT
    Row(verticalAlignment = Alignment.CenterVertically) {
        Txt("Productos", 15, weight = FontWeight.Bold, color = StaffInk, family = Inter, modifier = Modifier.weight(1f))
        Txt(
            if (draft) "Borrador" else "Aprobada",
            10,
            color = if (draft) StaffAccent else Emerald700,
            family = Inter,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (draft) StaffSoft else Emerald50).padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
    VSpace(10.dp)
    Column(Modifier.fillMaxWidth().staffCard().padding(horizontal = 16.dp, vertical = 8.dp)) {
        quotation.items.forEach {
            val label = if (it.quantity > 1) "${it.description} ×${it.quantity}" else it.description
            KeyValueRow(label, soles(it.unitPrice * it.quantity), 11, 11)
        }
        Divider(Lavender, thickness = 2.dp)
        KeyValueRow("Subtotal", soles(quotation.subtotal), 11, 11)
        quotation.discount?.let {
            val detail = if (it.type == "PERCENTAGE") "${it.value.toBigDecimal().stripTrailingZeros().toPlainString()} %" else "monto fijo"
            KeyValueRow("Descuento ($detail)", "-${soles(it.amount)}", 11, 11)
            it.reason?.let { reason -> Txt(reason, 10, color = Slate400, family = Inter) }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            Txt("Total", 16, color = StaffInk, family = Inter, modifier = Modifier.weight(1f))
            Txt(soles(quotation.total), 16, color = StaffInk, family = Inter)
        }
    }
    VSpace(20.dp)
    if (!draft) {
        Notice(
            if (state.sale == null) "El paciente aceptó la cotización. Ya se puede registrar la venta." else "La venta de esta cotización ya está registrada.",
            Emerald50,
            Emerald700,
        )
        VSpace(16.dp)
        StaffButton(
            when {
                state.isSaving -> "Registrando…"
                state.sale == null -> "Registrar venta"
                else -> "Ver venta"
            },
            { vm.startSale() },
            R.drawable.lucide_ic_credit_card,
        )
        return
    }
    Txt("Descuento o promoción", 15, weight = FontWeight.Bold, color = StaffInk, family = Inter)
    VSpace(10.dp)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StaffChip("Porcentaje", state.discountIsPercentage) { vm.setDiscountType(true) }
        StaffChip("Monto fijo (S/)", !state.discountIsPercentage) { vm.setDiscountType(false) }
    }
    VSpace(10.dp)
    StaffInput(state.discountValue, vm::setDiscountValue, if (state.discountIsPercentage) "Ej. 10" else "Ej. 50.00", keyboardType = KeyboardType.Decimal)
    VSpace(8.dp)
    StaffInput(state.discountReason, vm::setDiscountReason, "Motivo (opcional), ej. Campaña escolar")
    VSpace(10.dp)
    StaffButton("Aplicar descuento", { vm.applyDiscount() }, background = White, contentColor = StaffInk, border = StaffBorder)
    VSpace(24.dp)
    if (state.showReject) {
        StaffLabel("Motivo del rechazo")
        VSpace(6.dp)
        StaffInput(state.rejectReason, vm::setRejectReason, "Ej. El paciente lo pensará")
        VSpace(10.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StaffButton("Cancelar", { vm.showReject(false) }, background = White, contentColor = StaffInk, border = StaffBorder, modifier = Modifier.weight(1f))
            StaffButton(if (state.isSaving) "Guardando…" else "Rechazar", { vm.reject() }, background = ErrorRed, modifier = Modifier.weight(1f))
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StaffButton("Rechazar", { vm.showReject(true) }, background = White, contentColor = ErrorRed, border = StaffBorder, modifier = Modifier.weight(1f))
            StaffButton(if (state.isSaving) "Guardando…" else "Aprobar", { vm.approve() }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ChipRows(labels: List<String>, selected: (String) -> Boolean, onClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { label -> StaffChip(label, selected(label)) { onClick(label) } }
            }
        }
    }
}

@Composable
private fun Notice(text: String, background: androidx.compose.ui.graphics.Color, color: androidx.compose.ui.graphics.Color) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(background).padding(12.dp)) {
        Txt(text, 12, color = color, family = Inter, modifier = Modifier.width(0.dp).weight(1f))
    }
}
