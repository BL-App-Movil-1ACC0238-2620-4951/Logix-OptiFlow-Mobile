package com.logix.optiflow.presentation.staff.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.data.demo.ProductionStage
import com.logix.optiflow.presentation.navigation.Routes
import com.logix.optiflow.presentation.patient.components.image
import com.logix.optiflow.presentation.shared.components.Divider
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.StaffBackTopBar
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.staff.FrameStockViewModel
import com.logix.optiflow.presentation.staff.InventoryViewModel
import com.logix.optiflow.presentation.staff.MOVEMENT_DAYS
import com.logix.optiflow.presentation.staff.NewFrameViewModel
import com.logix.optiflow.presentation.staff.ReplenishViewModel
import com.logix.optiflow.presentation.staff.StaffFeature
import com.logix.optiflow.presentation.staff.can
import com.logix.optiflow.presentation.staff.components.StaffButton
import com.logix.optiflow.presentation.staff.components.StaffChip
import com.logix.optiflow.presentation.staff.components.StaffHero
import com.logix.optiflow.presentation.staff.components.StaffIconTile
import com.logix.optiflow.presentation.staff.components.StaffIndigoText
import com.logix.optiflow.presentation.staff.components.StaffInput
import com.logix.optiflow.presentation.staff.components.StaffLabel
import com.logix.optiflow.presentation.staff.components.StaffPill
import com.logix.optiflow.presentation.staff.components.StaffScreen
import com.logix.optiflow.presentation.staff.components.StaffSectionTitle
import com.logix.optiflow.presentation.staff.components.StaffSelect
import com.logix.optiflow.presentation.staff.components.StaffSoft
import com.logix.optiflow.presentation.staff.components.staffCard
import com.logix.optiflow.presentation.staff.components.staffViewModel
import com.logix.optiflow.presentation.staff.movementReason
import com.logix.optiflow.presentation.staff.movementTime
import com.logix.optiflow.presentation.staff.soles
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.ErrorBg
import com.logix.optiflow.presentation.theme.ErrorRed
import com.logix.optiflow.presentation.theme.Inter
import com.logix.optiflow.presentation.theme.Lavender
import com.logix.optiflow.presentation.theme.ScreenBg
import com.logix.optiflow.presentation.theme.Slate300
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.StaffAccent
import com.logix.optiflow.presentation.theme.StaffBorder
import com.logix.optiflow.presentation.theme.StaffChip
import com.logix.optiflow.presentation.theme.StaffGreen
import com.logix.optiflow.presentation.theme.StaffInk
import com.logix.optiflow.presentation.theme.White

private val ScanBg = Color(0xFF1F3A6D)
private val ScanPanel = Color(0xFF1E4383)

@Composable
fun ScannerScreen(nav: NavController) {
    com.logix.optiflow.presentation.shared.components.StatusBarIcons(lightIcons = true)
    var code by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(ScanBg).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            LIcon(R.drawable.lucide_ic_chevron_left, White, 24.dp, Modifier.tap { nav.popBackStack() })
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Txt("Escanear producto", 14, color = White, family = Inter)
                Txt("Por ahora escribe el SKU de la montura", 9, color = White.copy(alpha = 0.8f), family = Inter)
            }
            LIcon(R.drawable.lucide_ic_circle_question_mark, White, 22.dp)
        }
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(ScanPanel),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(width = 250.dp, height = 180.dp)) {
                val len = 34.dp.toPx()
                val stroke = 3.dp.toPx()
                val w = size.width
                val h = size.height
                listOf(
                    Offset(0f, 0f) to Pair(1f, 1f),
                    Offset(w, 0f) to Pair(-1f, 1f),
                    Offset(0f, h) to Pair(1f, -1f),
                    Offset(w, h) to Pair(-1f, -1f),
                ).forEach { (corner, dir) ->
                    drawLine(White, corner, Offset(corner.x + len * dir.first, corner.y), stroke, StrokeCap.Round)
                    drawLine(White, corner, Offset(corner.x, corner.y + len * dir.second), stroke, StrokeCap.Round)
                }
                drawLine(
                    brush = Brush.horizontalGradient(listOf(Color(0x66FFFFFF), White, Color(0x66FFFFFF))),
                    start = Offset(0f, h / 2),
                    end = Offset(w, h / 2),
                    strokeWidth = 3.dp.toPx(),
                )
            }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp)) {
            StaffInput(
                code,
                { code = it.uppercase().filter { c -> c.isLetterOrDigit() || c == '-' }.take(100) },
                "SKU, ej. RB-5154-BLK",
                icon = R.drawable.lucide_ic_pencil,
            )
            VSpace(12.dp)
            Box(
                Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).background(if (code.isBlank()) StaffAccent.copy(alpha = 0.5f) else StaffAccent).tap {
                    if (code.isNotBlank()) nav.navigate(Routes.product(code.trim()))
                },
                contentAlignment = Alignment.Center,
            ) {
                Txt("Ver producto", 15, weight = FontWeight.SemiBold, color = White, family = Inter)
            }
        }
    }
}

@Composable
fun ProductDetailScreen(nav: NavController) {
    val staff by staffViewModel().uiState.collectAsState()
    val vm: FrameStockViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()
    val role = staff.role
    LifecycleResumeEffect(staff.storeId) {
        vm.load(staff.storeId, withMovements = false)
        onPauseOrDispose {}
    }
    val frame = state.frame

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Detalle del producto", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(16.dp)
        if (frame == null) {
            Txt(if (state.isLoading) "Cargando…" else state.errorMessage.orEmpty(), 12, color = Slate400, family = Inter)
            return@StaffScreen
        }
        Column(Modifier.fillMaxWidth().staffCard(radius = 20.dp, border = White).padding(12.dp)) {
            Image(
                painter = painterResource(frame.image),
                contentDescription = frame.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(14.dp)),
            )
        }
        VSpace(16.dp)
        val quantity = frame.quantity ?: 0
        StaffPill(if (quantity == 0) "Sin stock en tu óptica" else "$quantity unidades disponibles")
        VSpace(12.dp)
        Txt(frame.name, 22, color = StaffInk, family = Inter)
        VSpace(6.dp)
        Txt(listOfNotNull(frame.sku, frame.color, frame.material).joinToString(" · "), 12, color = Slate400, family = Inter)
        VSpace(14.dp)
        Txt(soles(frame.price), 20, color = StaffInk, family = Inter)
        VSpace(18.dp)
        Column(Modifier.fillMaxWidth().staffCard(radius = 14.dp, background = Color(0xFFF7F9FF), border = Lavender)) {
            Row(Modifier.height(androidx.compose.foundation.layout.IntrinsicSize.Min)) {
                InfoCell("Material", frame.material ?: "—", Modifier.weight(1f))
                Box(Modifier.width(1.dp).fillMaxHeight().background(Lavender))
                InfoCell("Forma", frame.shape ?: "—", Modifier.weight(1f))
            }
            Divider(Lavender)
            Row(Modifier.height(androidx.compose.foundation.layout.IntrinsicSize.Min)) {
                InfoCell("Color", frame.color ?: "—", Modifier.weight(1f))
                Box(Modifier.width(1.dp).fillMaxHeight().background(Lavender))
                InfoCell("Stock mínimo", frame.minimumStock?.toString() ?: "—", Modifier.weight(1f))
            }
        }
        if (state.editingPrice) {
            VSpace(18.dp)
            StaffLabel("Nuevo precio (S/)")
            VSpace(8.dp)
            StaffInput(state.priceInput, vm::setPrice, "0.00", keyboardType = KeyboardType.Decimal)
            VSpace(12.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StaffButton("Cancelar", { vm.editPrice(false) }, background = White, contentColor = StaffInk, border = StaffBorder, modifier = Modifier.weight(1f))
                StaffButton(if (state.isSaving) "Guardando…" else "Guardar precio", { vm.savePrice() }, modifier = Modifier.weight(1f))
            }
        }
        state.message?.let {
            VSpace(12.dp)
            Txt(it, 12, color = StaffGreen, family = Inter)
        }
        state.errorMessage?.let {
            VSpace(12.dp)
            Txt(it, 12, color = ErrorRed, family = Inter)
        }
        VSpace(22.dp)
        val canStock = role.can(StaffFeature.INVENTORY)
        val canPrice = role.can(StaffFeature.CATALOG) && !state.editingPrice
        if (canStock || canPrice) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (canStock) {
                    StaffButton(
                        "Ver stock",
                        { nav.navigate(Routes.stock(frame.sku)) },
                        background = White,
                        contentColor = StaffInk,
                        border = StaffBorder,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (canPrice) {
                    StaffButton("Cambiar precio", { vm.editPrice(true) }, background = White, contentColor = StaffInk, border = StaffBorder, modifier = Modifier.weight(1f))
                }
            }
            VSpace(12.dp)
        }
        if (role.can(StaffFeature.QUOTES)) StaffButton("Crear cotización", { nav.navigate(Routes.S_QUOTE) })
        VSpace(24.dp)
    }
}

@Composable
private fun InfoCell(label: String, value: String, modifier: Modifier) {
    Column(modifier.padding(14.dp)) {
        Txt(label, 9, color = StaffInk, family = Inter)
        VSpace(6.dp)
        Txt(value, 11, color = StaffInk, family = Inter)
    }
}

@Composable
fun StockManagementScreen(nav: NavController) {
    val staff by staffViewModel().uiState.collectAsState()
    val vm: FrameStockViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()
    LifecycleResumeEffect(staff.storeId) {
        vm.load(staff.storeId, withMovements = true)
        onPauseOrDispose {}
    }
    val frame = state.frame

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Detalle y gestión de stock", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(16.dp)
        if (frame == null) {
            Txt(if (state.isLoading) "Cargando…" else state.errorMessage.orEmpty(), 12, color = Slate400, family = Inter)
            return@StaffScreen
        }
        val stock = frame.quantity ?: 0
        val minimum = frame.minimumStock
        val low = minimum != null && stock <= minimum
        StaffHero(radius = 20.dp, padding = PaddingValues(16.dp, 18.dp)) {
            Txt("STOCK EN ${staff.storeName.uppercase()}", 8, color = Slate300, family = Inter, maxLines = 1)
            VSpace(14.dp)
            Txt(frame.name, 20, color = White, family = Inter)
            Txt(listOfNotNull(frame.color, frame.material, frame.shape).joinToString(" · "), 9, color = Slate300, family = Inter)
            VSpace(16.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(frame.image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(width = 68.dp, height = 44.dp).clip(RoundedCornerShape(8.dp)),
                )
                HSpace(12.dp)
                Column(Modifier.weight(1f)) {
                    Txt("SKU", 7, color = Slate300, family = Inter)
                    Txt(frame.sku, 16, color = White, family = Inter)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Txt("PRECIO", 7, color = Slate300, family = Inter)
                    Txt(soles(frame.price), 12, color = White, family = Inter)
                }
            }
        }
        VSpace(12.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StockStat(
                R.drawable.lucide_ic_box,
                if (low) ErrorBg else Color(0xFFEEF2FF),
                if (low) ErrorRed else StaffAccent,
                if (low) "Bajo" else "OK",
                "$stock",
                if (low) ErrorRed else StaffInk,
                "Stock actual",
                "Unidades en tu óptica",
                Modifier.weight(1f),
            )
            StockStat(
                R.drawable.lucide_ic_file_text,
                Color(0xFFEEF2FF),
                StaffAccent,
                "Mínimo",
                minimum?.toString() ?: "—",
                StaffInk,
                "Stock mínimo",
                when {
                    minimum == null -> "Se fija en la primera reposición"
                    low -> "Faltan ${minimum - stock + 1} uds para salir de la alerta"
                    else -> "${stock - minimum} uds sobre el mínimo"
                },
                Modifier.weight(1f),
            )
        }
        VSpace(24.dp)
        StaffSectionTitle("Movimientos · últimos $MOVEMENT_DAYS días")
        VSpace(12.dp)
        if (state.movements.isEmpty()) {
            Txt("Sin movimientos en este periodo.", 12, color = Slate400, family = Inter)
        }
        state.movements.forEach { movement ->
            MovementRow(
                if (movement.isEntry) R.drawable.lucide_ic_box else R.drawable.lucide_ic_credit_card,
                movementReason(movement),
                movementTime(movement.createdAt),
                (if (movement.isEntry) "+" else "-") + "${movement.quantity} ud" + (if (movement.quantity == 1) "" else "s"),
                if (movement.isEntry) StaffGreen else ErrorRed,
            )
            VSpace(10.dp)
        }
        VSpace(16.dp)
        StaffButton("Registrar reposición", { nav.navigate(Routes.movement(frame.sku)) }, R.drawable.lucide_ic_plus)
        VSpace(24.dp)
    }
}

@Composable
private fun StockStat(
    icon: Int,
    tileBg: Color,
    tint: Color,
    pill: String,
    value: String,
    valueColor: Color,
    title: String,
    subtitle: String,
    modifier: Modifier,
) {
    Column(modifier.staffCard().padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StaffIconTile(icon, size = 32.dp, background = tileBg, tint = tint, radius = 10.dp)
            Spacer(Modifier.weight(1f))
            StaffPill(pill, size = 8)
        }
        VSpace(16.dp)
        Txt(value, 24, color = valueColor, family = Inter)
        Txt(title, 13, color = StaffInk, family = Inter)
        Txt(subtitle, 8, color = Slate400, family = Inter)
    }
}

@Composable
private fun MovementRow(icon: Int, title: String, subtitle: String, delta: String, color: Color) {
    Row(Modifier.fillMaxWidth().staffCard().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        StaffIconTile(icon, size = 32.dp, background = Color(0xFFEEF2FF), radius = 10.dp)
        HSpace(12.dp)
        Column(Modifier.weight(1f)) {
            Txt(title, 10, color = StaffInk, family = Inter)
            Txt(subtitle, 7, color = StaffInk, family = Inter)
        }
        Txt(delta, 11, color = color, family = Inter)
    }
}

@Composable
fun NewMovementScreen(nav: NavController) {
    val staff by staffViewModel().uiState.collectAsState()
    val vm: ReplenishViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(staff.storeId) { vm.load(staff.storeId) }
    LaunchedEffect(state.done) {
        if (state.done) {
            Toast.makeText(context, "Reposición registrada", Toast.LENGTH_SHORT).show()
            nav.popBackStack()
        }
    }
    val selected = state.selected
    val current = selected?.stock?.quantity ?: 0

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Reposición de stock", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(24.dp)
        if (state.isLoading) {
            Txt("Cargando…", 12, color = Slate400, family = Inter)
            return@StaffScreen
        }
        Txt("Montura", 13, color = StaffInk, family = Inter)
        VSpace(10.dp)
        StaffSelect(selected?.let { "${it.frame.name} · ${it.frame.sku}" } ?: "Elige una montura", null, onClick = {
            val rows = state.rows
            if (rows.isNotEmpty()) {
                val index = rows.indexOfFirst { it.frame.sku == state.sku }
                vm.selectSku(rows[(index + 1) % rows.size].frame.sku)
            }
        })
        VSpace(22.dp)
        Txt("Proveedor", 13, color = StaffInk, family = Inter)
        VSpace(10.dp)
        StaffSelect(state.supplier?.name ?: "Sin proveedores", null, onClick = {
            val suppliers = state.suppliers
            if (suppliers.isNotEmpty()) {
                val index = suppliers.indexOfFirst { it.id == state.supplierId }
                vm.selectSupplier(suppliers[(index + 1) % suppliers.size].id)
            }
        })
        VSpace(22.dp)
        Txt("Unidades que ingresan", 13, color = StaffInk, family = Inter)
        VSpace(10.dp)
        Row(Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).background(StaffSoft).border(1.dp, Lavender, RoundedCornerShape(14.dp))) {
            Box(Modifier.width(48.dp).fillMaxHeight().tap { vm.changeQuantity(-1) }, contentAlignment = Alignment.Center) {
                Txt("−", 18, color = StaffAccent, family = Inter)
            }
            Box(Modifier.weight(1f).fillMaxHeight().background(White.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                Txt("${state.quantity}", 16, color = StaffInk, family = Inter)
            }
            Box(Modifier.width(48.dp).fillMaxHeight().tap { vm.changeQuantity(1) }, contentAlignment = Alignment.Center) {
                Txt("+", 18, color = StaffAccent, family = Inter)
            }
        }
        VSpace(22.dp)
        StaffLabel("Stock mínimo", "Opcional")
        VSpace(8.dp)
        StaffInput(
            state.minimumInput,
            vm::setMinimum,
            selected?.stock?.let { "Actual: ${it.minimumStock}" } ?: "5 por defecto",
            keyboardType = KeyboardType.Number,
        )
        VSpace(18.dp)
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(StaffSoft).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            StaffIconTile(R.drawable.lucide_ic_layout_grid, size = 36.dp, background = Lavender, tint = Electric, radius = 10.dp)
            HSpace(12.dp)
            Column {
                Txt("Stock resultante: ${current + state.quantity} unidades", 11, color = StaffInk, family = Inter)
                Txt("Stock actual: $current unidades", 9, color = StaffIndigoText, family = Inter)
            }
        }
        state.errorMessage?.let {
            VSpace(12.dp)
            Txt(it, 12, color = ErrorRed, family = Inter)
        }
        VSpace(32.dp)
        StaffButton(if (state.isSaving) "Registrando…" else "Registrar reposición", { vm.submit() })
        VSpace(24.dp)
    }
}

@Composable
fun NewFrameScreen(nav: NavController) {
    val staff by staffViewModel().uiState.collectAsState()
    val vm: NewFrameViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(staff.storeId, staff.role) { vm.load(staff.storeId, staff.role.can(StaffFeature.INVENTORY)) }
    LaunchedEffect(state.createdSku) {
        val sku = state.createdSku ?: return@LaunchedEffect
        Toast.makeText(context, state.errorMessage ?: "Montura registrada", Toast.LENGTH_LONG).show()
        nav.navigate(Routes.product(sku)) { popUpTo(Routes.S_NEW_FRAME) { inclusive = true } }
    }

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Nueva montura", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(20.dp)
        FrameField("SKU", state.sku, vm::setSku, "Ej. RB-2140-BLK")
        FrameField("Nombre", state.name, vm::setName, "Ej. Ray-Ban Wayfarer")
        FrameField("Material", state.material, vm::setMaterial, "Ej. Acetato", optional = true)
        FrameField("Forma", state.shape, vm::setShape, "Ej. Cuadrada", optional = true)
        FrameField("Color", state.color, vm::setColor, "Ej. Negro", optional = true)
        FrameField("Precio (S/)", state.price, vm::setPrice, "0.00", keyboardType = KeyboardType.Decimal)
        if (state.withStock) {
            VSpace(8.dp)
            Txt("Stock inicial en tu óptica", 16, weight = FontWeight.Bold, color = StaffInk, family = Inter)
            VSpace(14.dp)
            FrameField("Unidades", state.quantity, vm::setQuantity, "0 para ingresarlas después", optional = true, keyboardType = KeyboardType.Number)
            FrameField("Stock mínimo", state.minimum, vm::setMinimum, "5 por defecto", optional = true, keyboardType = KeyboardType.Number)
            Txt("Proveedor", 13, color = StaffInk, family = Inter)
            VSpace(8.dp)
            StaffSelect(state.supplier?.name ?: "Sin proveedores", null, onClick = vm::nextSupplier)
            VSpace(16.dp)
        }
        if (state.createdSku == null) {
            state.errorMessage?.let {
                Txt(it, 12, color = ErrorRed, family = Inter)
                VSpace(12.dp)
            }
        }
        VSpace(12.dp)
        StaffButton(if (state.isSaving) "Registrando…" else "Registrar montura", { vm.submit() })
        VSpace(24.dp)
    }
}

@Composable
private fun FrameField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    optional: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    StaffLabel(label, if (optional) "Opcional" else null)
    VSpace(8.dp)
    StaffInput(value, onChange, placeholder, keyboardType = keyboardType)
    VSpace(16.dp)
}

private class AlertUi(val kind: String, val title: String, val message: String, val action: String, val icon: Int, val route: String)

@Composable
fun AlertsScreen(nav: NavController) {
    val staff by staffViewModel().uiState.collectAsState()
    val vm: InventoryViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()
    LifecycleResumeEffect(staff.storeId) {
        vm.loadAlerts(staff.storeId)
        onPauseOrDispose {}
    }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val open = staff.productionOrders.filter { it.stage != ProductionStage.DELIVERED }
    val ready = open.count { it.stage == ProductionStage.READY_FOR_DELIVERY }
    val all =
        state.alerts.map { item ->
            AlertUi(
                "Inventario",
                if (item.quantity == 0) "Sin stock" else "Stock bajo",
                "${item.name} tiene ${item.quantity} ${if (item.quantity == 1) "unidad" else "unidades"} (mínimo ${item.minimumStock}).",
                "Reponer",
                R.drawable.lucide_ic_bell,
                Routes.stock(item.sku),
            )
        } +
            open.filter { it.needsAttention && it.id != null }.map { order ->
                AlertUi("Producción", "Orden con retraso", "${order.code} de ${order.patient}: ${order.due}.", "Ver orden", R.drawable.lucide_ic_clock, Routes.workOrder(order.id!!))
            } +
            listOfNotNull(
                AlertUi(
                    "Producción",
                    "Pedidos listos",
                    if (ready == 1) "1 pedido espera ser entregado." else "$ready pedidos esperan ser entregados.",
                    "Ver pedidos",
                    R.drawable.lucide_ic_box,
                    Routes.S_PRODUCTION,
                ).takeIf { ready > 0 },
            )
    val alerts =
        all.filter {
            when (filter) {
                1 -> it.kind == "Inventario"
                2 -> it.kind == "Producción"
                else -> true
            }
        }
    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Alertas", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(20.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Todas", "Inventario", "Producción").forEachIndexed { index, label ->
                StaffChip(label, filter == index) { filter = index }
            }
        }
        VSpace(16.dp)
        when {
            state.isLoading -> Txt("Cargando…", 12, color = Slate400, family = Inter)
            state.errorMessage != null -> Txt(state.errorMessage.orEmpty(), 12, color = Slate400, family = Inter)
            alerts.isEmpty() -> Txt("Sin alertas por ahora.", 12, color = Slate400, family = Inter)
        }
        alerts.forEach { alert ->
            Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                Row {
                    StaffIconTile(alert.icon, size = 34.dp, background = StaffSoft, radius = 10.dp)
                    HSpace(14.dp)
                    Column(Modifier.weight(1f)) {
                        Txt(alert.title, 12, color = StaffInk, family = Inter)
                        VSpace(8.dp)
                        Txt(alert.message, 10, color = StaffIndigoText, family = Inter)
                    }
                }
                VSpace(8.dp)
                Row(
                    Modifier.align(Alignment.End).padding(end = 120.dp).tap { nav.navigate(alert.route) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Txt(alert.action, 9, color = StaffAccent, family = Inter)
                    HSpace(4.dp)
                    LIcon(R.drawable.lucide_ic_chevron_right, StaffAccent, 14.dp)
                }
            }
            Divider(StaffBorder)
        }
    }
}
