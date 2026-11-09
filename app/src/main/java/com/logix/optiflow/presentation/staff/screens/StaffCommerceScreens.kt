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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.data.demo.DemoData
import com.logix.optiflow.presentation.navigation.Routes
import com.logix.optiflow.presentation.shared.components.Divider
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.StaffBackTopBar
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.staff.components.KeyValueRow
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
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.ErrorBg
import com.logix.optiflow.presentation.theme.ErrorRed
import com.logix.optiflow.presentation.theme.Inter
import com.logix.optiflow.presentation.theme.Lavender
import com.logix.optiflow.presentation.theme.MintWhite
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
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().background(ScanBg).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            LIcon(R.drawable.lucide_ic_chevron_left, White, 24.dp, Modifier.tap { nav.popBackStack() })
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Txt("Escanear producto", 14, color = White, family = Inter)
                Txt("Alinea el código dentro del marco", 9, color = White.copy(alpha = 0.8f), family = Inter)
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
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 70.dp)
                    .shadow(6.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(White)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(22.dp).clip(CircleShape).background(Electric), contentAlignment = Alignment.Center) {
                    LIcon(R.drawable.lucide_ic_check, White, 13.dp)
                }
                HSpace(10.dp)
                Column {
                    Txt("Código detectado", 11, color = StaffInk, family = Inter)
                    Txt("NV-N24-AZ", 8, color = StaffIndigoText, family = Inter)
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.tap {
                Toast.makeText(context, "Linterna activada", Toast.LENGTH_SHORT).show()
            }) {
                LIcon(R.drawable.lucide_ic_camera, White, 22.dp)
                VSpace(6.dp)
                Txt("Linterna", 8, color = White, family = Inter)
            }
            HSpace(26.dp)
            Box(
                Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(14.dp)).background(StaffAccent).tap {
                    nav.navigate(Routes.product("rb5154"))
                },
                contentAlignment = Alignment.Center,
            ) {
                Txt("Ver producto", 15, weight = FontWeight.SemiBold, color = White, family = Inter)
            }
            HSpace(16.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LIcon(R.drawable.lucide_ic_pencil, White, 22.dp)
                VSpace(6.dp)
                Txt("Ingresar código", 8, color = White, family = Inter)
            }
        }
    }
}

@Composable
fun ProductDetailScreen(nav: NavController, id: String) {
    val product = DemoData.product(id)
    val vm = staffViewModel()
    val state by vm.uiState.collectAsState()
    var thumb by rememberSaveable { mutableIntStateOf(0) }
    val gallery = listOf(product.image, R.drawable.img_prod_thumb2, R.drawable.img_eval_frame)

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Detalle del producto", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(16.dp)
        Column(Modifier.fillMaxWidth().staffCard(radius = 20.dp, border = White).padding(12.dp)) {
            Image(
                painter = painterResource(gallery[thumb]),
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(14.dp)),
            )
            VSpace(12.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                gallery.forEachIndexed { index, res ->
                    Image(
                        painter = painterResource(res),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(if (index == thumb) 1.5.dp else 0.dp, if (index == thumb) StaffAccent else Color.Transparent, RoundedCornerShape(10.dp))
                                .tap { thumb = index },
                    )
                }
            }
        }
        VSpace(16.dp)
        StaffPill("${state.stock[product.id] ?: product.available} unidades disponibles")
        VSpace(12.dp)
        Txt(product.name, 22, color = StaffInk, family = Inter)
        VSpace(6.dp)
        Txt("${product.sku} · Carey y dorado · Acetato con metal", 12, color = Slate400, family = Inter)
        VSpace(14.dp)
        Txt(product.price, 20, color = StaffInk, family = Inter)
        VSpace(18.dp)
        Column(Modifier.fillMaxWidth().staffCard(radius = 14.dp, background = Color(0xFFF7F9FF), border = Lavender)) {
            Row(Modifier.height(androidx.compose.foundation.layout.IntrinsicSize.Min)) {
                InfoCell("Código", product.code, Modifier.weight(1f))
                Box(Modifier.width(1.dp).fillMaxHeight().background(Lavender))
                InfoCell("Talla", product.size.replace("-", "–"), Modifier.weight(1f))
            }
            Divider(Lavender)
            Row(Modifier.height(androidx.compose.foundation.layout.IntrinsicSize.Min)) {
                InfoCell("Ubicación", product.location, Modifier.weight(1f))
                Box(Modifier.width(1.dp).fillMaxHeight().background(Lavender))
                InfoCell("Proveedor", product.supplier, Modifier.weight(1f))
            }
        }
        VSpace(22.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StaffButton(
                "Ver stock",
                { nav.navigate(Routes.stock(product.id)) },
                background = White,
                contentColor = StaffInk,
                border = StaffBorder,
                modifier = Modifier.weight(0.8f),
            )
            StaffButton("Agregar", { nav.navigate(Routes.S_SALE) }, modifier = Modifier.weight(1.2f))
        }
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
fun StockManagementScreen(nav: NavController, id: String) {
    val product = DemoData.product(id)
    val vm = staffViewModel()
    val state by vm.uiState.collectAsState()
    val stock = state.stock[product.id] ?: product.available
    val context = LocalContext.current

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Detalle y gestión de stock", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(16.dp)
        StaffHero(radius = 20.dp, padding = PaddingValues(16.dp, 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Txt("CONTROL DE LOTE · REF #${product.sku}", 8, color = Slate300, family = Inter, modifier = Modifier.weight(1f))
                StaffPill(product.supplier, background = White, color = StaffInk, size = 8)
            }
            VSpace(14.dp)
            Txt(product.name, 20, color = White, family = Inter)
            Txt("${product.sku} 2000 · Carey & Dorado Vintage (Acetato + Metal)", 9, color = Slate300, family = Inter)
            VSpace(16.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(product.image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(width = 68.dp, height = 44.dp).clip(RoundedCornerShape(8.dp)),
                )
                HSpace(12.dp)
                Column(Modifier.weight(1f)) {
                    Txt("SKU PRIMARIO", 7, color = Slate300, family = Inter)
                    Txt(product.code, 16, color = White, family = Inter)
                    Txt("Calibre 51 · Puente 21 · Varilla 145", 8, color = Slate300, family = Inter)
                }
                LIcon(R.drawable.lucide_ic_scan, White, 20.dp, Modifier.tap { nav.navigate(Routes.S_SCANNER) })
            }
            VSpace(18.dp)
            Row {
                listOf("PVP SUGERIDO" to product.price, "COSTO UNIT." to "$98.500", "MARGEN" to "48% Bruto").forEachIndexed { index, (label, value) ->
                    Column(Modifier.weight(1f), horizontalAlignment = if (index == 2) Alignment.End else Alignment.Start) {
                        Txt(label, 7, color = Slate300, family = Inter)
                        Txt(value, 12, color = White, family = Inter)
                    }
                }
            }
        }
        VSpace(12.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StockStat(R.drawable.lucide_ic_box, ErrorBg, ErrorRed, "Bajo", "$stock", ErrorRed, "Stock actual", "Total físico red", Modifier.weight(1f))
            StockStat(R.drawable.lucide_ic_file_text, Color(0xFFEEF2FF), StaffAccent, "Objetivo", "6", StaffInk, "Mínimo umbral", "Déficit: ${stock - 6} uds", Modifier.weight(1f))
        }
        VSpace(10.dp)
        Row(Modifier.fillMaxWidth().staffCard().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            StaffIconTile(R.drawable.lucide_ic_box, size = 34.dp, background = Color(0xFFEEF2FF), radius = 10.dp)
            HSpace(12.dp)
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Txt("12 en tránsito", 12, color = StaffInk, family = Inter)
                    HSpace(8.dp)
                    StaffPill("Hoy 16:30", size = 8)
                }
                Txt("Despacho Guía #EX-90412 · Essilor Central", 8, color = StaffInk, family = Inter)
            }
            LIcon(R.drawable.lucide_ic_chevron_right, StaffInk, 18.dp)
        }
        VSpace(10.dp)
        Row(Modifier.fillMaxWidth().staffCard().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Txt("Ajuste manual rápido", 11, color = StaffInk, family = Inter)
                Txt("Registrar merma, rotura o conteo", 8, color = StaffInk, family = Inter)
            }
            StepButton("−") { vm.adjustStock(product.id, -1) }
            Txt("$stock", 18, color = StaffInk, family = Inter, modifier = Modifier.padding(horizontal = 14.dp))
            StepButton("+") { vm.adjustStock(product.id, 1) }
        }
        VSpace(24.dp)
        StaffSectionTitle("Disponibilidad en red", "3 puntos")
        VSpace(12.dp)
        Column(Modifier.fillMaxWidth().staffCard().padding(horizontal = 14.dp)) {
            NetworkRow(R.drawable.lucide_ic_layout_grid, "Vitrina Sucursal Centro", "2 unidades (Bajo mínimo)", StaffAccent, "Auditar")
            Divider(StaffBorder)
            NetworkRow(R.drawable.lucide_ic_house, "Cajón Taller / Bodega", "2 unidades operativas", StaffAccent, "Mover")
            Divider(StaffBorder)
            NetworkRow(R.drawable.lucide_ic_box, "Sucursal Norte", "0 unidades (Agotado)", ErrorRed, "Sin stock")
        }
        VSpace(24.dp)
        StaffSectionTitle("Acciones directas")
        VSpace(12.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DirectAction(R.drawable.lucide_ic_chevron_right, "Trasladar", "Enviar a otra sucursal", Modifier.weight(1f)) {
                nav.navigate(Routes.S_MOVEMENT)
            }
            DirectAction(R.drawable.lucide_ic_file_text, "Etiqueta", "Imprimir código barras", Modifier.weight(1f)) {
                Toast.makeText(context, "Etiqueta enviada a la impresora", Toast.LENGTH_SHORT).show()
            }
        }
        VSpace(24.dp)
        StaffSectionTitle("Últimos movimientos", "Ver kardex")
        VSpace(12.dp)
        MovementRow(R.drawable.lucide_ic_credit_card, "Venta en tienda #8839", "Boleta B-1092 · Hoy, 11:20 · Vendedora: Camila R.", "-1 ud", ErrorRed)
        VSpace(10.dp)
        MovementRow(R.drawable.lucide_ic_box, "Recepción lote Essilor", "Factura Prov #77102 · 18 jun, 09:45", "+5 uds", StaffGreen)
        VSpace(16.dp)
        StaffButton("Solicitar pedido de reposición", {
            Toast.makeText(context, "Pedido de reposición solicitado", Toast.LENGTH_SHORT).show()
        }, R.drawable.lucide_ic_plus)
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
private fun StepButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFEEF2FF)).tap(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Txt(label, 16, color = StaffAccent, family = Inter)
    }
}

@Composable
private fun NetworkRow(icon: Int, title: String, subtitle: String, subtitleColor: Color, action: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        StaffIconTile(icon, size = 34.dp, background = Color(0xFFEEF2FF), radius = 10.dp)
        HSpace(12.dp)
        Column(Modifier.weight(1f)) {
            Txt(title, 11, color = StaffInk, family = Inter)
            Txt(subtitle, 8, color = subtitleColor, family = Inter)
        }
        StaffPill(action, size = 8)
    }
}

@Composable
private fun DirectAction(icon: Int, title: String, subtitle: String, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.staffCard().tap(onClick).padding(14.dp)) {
        StaffIconTile(icon, size = 30.dp, background = Color(0xFFEEF2FF), radius = 8.dp)
        VSpace(10.dp)
        Txt(title, 11, color = StaffInk, family = Inter)
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

private val movementReasons = listOf("Reposición de proveedor", "Devolución de cliente", "Traslado entre sucursales", "Merma o rotura")

@Composable
fun NewMovementScreen(nav: NavController) {
    val vm = staffViewModel()
    var type by rememberSaveable { mutableIntStateOf(0) }
    var qty by rememberSaveable { mutableIntStateOf(1) }
    var reason by rememberSaveable { mutableIntStateOf(0) }
    var note by rememberSaveable { mutableStateOf("") }
    val current = 12
    val result = when (type) {
        0 -> current + qty
        1 -> current - qty
        else -> qty
    }
    val context = LocalContext.current

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Nuevo movimiento", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(30.dp)
        Txt("Tipo de movimiento", 16, weight = FontWeight.Bold, color = StaffInk, family = Inter)
        VSpace(14.dp)
        Row(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(14.dp)).background(StaffSoft).padding(4.dp)) {
            listOf("Ingreso", "Salida", "Ajuste").forEachIndexed { index, label ->
                val selected = index == type
                Box(
                    Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(10.dp)).background(if (selected) White else Color.Transparent).tap { type = index },
                    contentAlignment = Alignment.Center,
                ) {
                    Txt(label, 10, color = if (selected) StaffAccent else StaffInk, family = Inter)
                }
            }
        }
        VSpace(22.dp)
        Txt("Cantidad", 13, color = StaffInk, family = Inter)
        VSpace(10.dp)
        Row(Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).background(StaffSoft).border(1.dp, Lavender, RoundedCornerShape(14.dp))) {
            Box(Modifier.width(48.dp).fillMaxHeight().tap { qty = (qty - 1).coerceAtLeast(1) }, contentAlignment = Alignment.Center) {
                Txt("−", 18, color = StaffAccent, family = Inter)
            }
            Box(Modifier.weight(1f).fillMaxHeight().background(White.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                Txt("$qty", 16, color = StaffInk, family = Inter)
            }
            Box(Modifier.width(48.dp).fillMaxHeight().tap { qty += 1 }, contentAlignment = Alignment.Center) {
                Txt("+", 18, color = StaffAccent, family = Inter)
            }
        }
        VSpace(22.dp)
        Txt("Motivo", 13, color = StaffInk, family = Inter)
        VSpace(10.dp)
        StaffSelect(movementReasons[reason], null, onClick = { reason = (reason + 1) % movementReasons.size })
        VSpace(22.dp)
        StaffLabel("Nota", "Opcional")
        VSpace(8.dp)
        StaffInput(note, { note = it }, "Agrega un detalle del movimiento...", height = 82.dp, singleLine = false)
        VSpace(18.dp)
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(StaffSoft).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            StaffIconTile(R.drawable.lucide_ic_layout_grid, size = 36.dp, background = Lavender, tint = Electric, radius = 10.dp)
            HSpace(12.dp)
            Column {
                Txt("Stock resultante: $result unidades", 11, color = StaffInk, family = Inter)
                Txt("Stock actual: $current unidades", 9, color = StaffIndigoText, family = Inter)
            }
        }
        VSpace(32.dp)
        StaffButton("Confirmar movimiento", {
            val delta = when (type) {
                0 -> qty
                1 -> -qty
                else -> 0
            }
            vm.adjustStock("rb5154", delta)
            Toast.makeText(context, "Movimiento registrado", Toast.LENGTH_SHORT).show()
            nav.popBackStack()
        })
        VSpace(24.dp)
    }
}

@Composable
private fun PatientStrip(caption: String, badge: Boolean) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(StaffSoft).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StaffInitials("VS", size = 42.dp, fontSize = 13)
        HSpace(12.dp)
        Column(Modifier.weight(1f)) {
            Txt("Paciente", 9, color = StaffInk, family = Inter)
            Txt("Valentina Silva", 12, color = StaffInk, family = Inter)
            Txt(caption, 9, color = StaffAccent, family = Inter)
        }
        if (badge) StaffPill("Vigente", background = Color.Transparent, size = 10)
    }
}

@Composable
private fun TotalsCard(subtotal: String, discount: String, total: String) {
    Column(Modifier.fillMaxWidth().staffCard().padding(horizontal = 16.dp, vertical = 8.dp)) {
        KeyValueRow("Subtotal", subtotal, labelSize = 10, valueSize = 10)
        KeyValueRow("Descuento", discount, labelSize = 10, valueSize = 10)
        Divider(Lavender, thickness = 2.dp)
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            Txt("Total", 16, color = StaffInk, family = Inter, modifier = Modifier.weight(1f))
            Txt(total, 16, color = StaffInk, family = Inter)
        }
    }
}

@Composable
private fun LineItem(title: String, subtitle: String, price: String, priceColor: Color = StaffInk, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).background(StaffChip), contentAlignment = Alignment.Center) {
            LIcon(R.drawable.lucide_ic_eye, StaffInk, 22.dp)
        }
        HSpace(12.dp)
        Column(Modifier.weight(1f)) {
            Txt(title, 11, color = StaffInk, family = Inter)
            Txt(subtitle, 8, color = StaffInk, family = Inter)
            if (trailing != null) {
                VSpace(4.dp)
                Txt(price, 12, color = priceColor, family = Inter)
            }
        }
        if (trailing != null) trailing() else Txt(price, 10, color = StaffInk, family = Inter)
    }
}

@Composable
fun QuoteScreen(nav: NavController) {
    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Nueva cotización", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(16.dp)
        PatientStrip("Receta del 12 mar 2025", badge = true)
        VSpace(24.dp)
        Txt("Productos", 17, weight = FontWeight.Bold, color = StaffInk, family = Inter)
        VSpace(12.dp)
        Box(Modifier.fillMaxWidth().height(64.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = Electric,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                    ),
                )
            }
            Row(
                Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)).background(Color(0xFFF7F9FF)).tap { nav.navigate(Routes.S_INVENTORY) }.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Electric), contentAlignment = Alignment.Center) {
                    LIcon(R.drawable.lucide_ic_plus, White, 20.dp)
                }
                HSpace(12.dp)
                Column(Modifier.weight(1f)) {
                    Txt("Agregar montura o lente", 12, color = StaffInk, family = Inter)
                    Txt("Busca en el catálogo o escanea", 9, color = StaffInk, family = Inter)
                }
                LIcon(R.drawable.lucide_ic_chevron_right, StaffInk, 20.dp)
            }
        }
        VSpace(6.dp)
        LineItem("Lentes monofocales", "Índice 1.60 · Antirreflejo", "$89.900", StaffAccent) {
            Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(White), contentAlignment = Alignment.Center) {
                LIcon(R.drawable.lucide_ic_pencil, StaffInk, 18.dp)
            }
        }
        Divider(Lavender)
        VSpace(16.dp)
        TotalsCard("$89.900", "—", "$89.900")
        VSpace(40.dp)
        StaffButton("Agregar montura", { nav.navigate(Routes.S_SCANNER) })
        VSpace(24.dp)
    }
}

@Composable
fun ConfirmSaleScreen(nav: NavController) {
    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Confirmar venta", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(16.dp)
        PatientStrip("Venta asociada a receta", badge = false)
        VSpace(24.dp)
        Txt("Resumen", 17, weight = FontWeight.Bold, color = StaffInk, family = Inter)
        VSpace(12.dp)
        Column(Modifier.fillMaxWidth().staffCard().padding(horizontal = 14.dp, vertical = 4.dp)) {
            LineItem("Nova N-24", "Montura · Azul marino", "$72.900")
            Divider(Lavender)
            LineItem("Lentes monofocales", "1.60 · Antirreflejo", "$89.900")
        }
        VSpace(16.dp)
        TotalsCard("$162.800", "-$10.000", "$152.800")
        VSpace(22.dp)
        Txt("Entrega estimada", 13, color = StaffInk, family = Inter)
        VSpace(10.dp)
        StaffSelect("28 de junio de 2025", R.drawable.lucide_ic_calendar, onClick = {})
        VSpace(40.dp)
        StaffButton("Continuar al pago", { nav.navigate(Routes.S_SALE_DONE) })
        VSpace(24.dp)
    }
}

@Composable
fun SaleDoneScreen(nav: NavController) {
    com.logix.optiflow.presentation.shared.components.StatusBarIcons(lightIcons = false)
    Column(
        Modifier.fillMaxSize().background(MintWhite).statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        VSpace(70.dp)
        Box(contentAlignment = Alignment.Center) {
            Box(Modifier.size(200.dp).clip(CircleShape).background(Brush.radialGradient(listOf(Color(0xFFE6ECFF), MintWhite))))
            Box(Modifier.size(124.dp).clip(CircleShape).border(1.dp, Color(0xFFDDE5FB), CircleShape))
            Box(Modifier.size(104.dp).clip(CircleShape).border(1.dp, Color(0xFFDDE5FB), CircleShape))
            Box(Modifier.size(88.dp).clip(CircleShape).background(Electric), contentAlignment = Alignment.Center) {
                LIcon(R.drawable.lucide_ic_check, White, 36.dp)
            }
        }
        VSpace(8.dp)
        Txt("VENTA #V-1038", 10, color = StaffAccent, family = Inter, letterSpacing = 1.5.sp)
        VSpace(12.dp)
        Txt("Venta confirmada", 26, color = StaffInk, family = Inter)
        VSpace(12.dp)
        Txt("El pago fue registrado correctamente.", 14, color = StaffIndigoText, family = Inter)
        VSpace(26.dp)
        Column(Modifier.fillMaxWidth().staffCard(radius = 20.dp, border = Lavender).padding(horizontal = 16.dp, vertical = 4.dp)) {
            KeyValueRow("Paciente", "Valentina Silva", 10, 10)
            Divider(Lavender)
            KeyValueRow("Total", "$152.800", 10, 10)
            Divider(Lavender)
            KeyValueRow("Pago", "Tarjeta", 10, 10)
        }
        VSpace(28.dp)
        StaffButton("Volver al inicio", { nav.navigate(Routes.S_HOME) { popUpTo(Routes.S_HOME) { inclusive = true } } })
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun AlertsScreen(nav: NavController) {
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val alerts =
        DemoData.alerts.filter {
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
                    Modifier.align(Alignment.End).padding(end = 120.dp).tap {
                        if (alert.kind == "Inventario") nav.navigate(Routes.stock("rb5154")) else nav.navigate(Routes.S_PRODUCTION)
                    },
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

@Composable
fun ReportsScreen(nav: NavController) {
    val context = LocalContext.current
    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Reportes", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(16.dp)
        Row(Modifier.fillMaxWidth().height(50.dp).staffCard(radius = 16.dp, border = Lavender).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Txt("Este mes", 10, color = StaffInk, family = Inter)
            HSpace(10.dp)
            Txt("Junio 2025", 12, color = StaffInk, family = Inter, modifier = Modifier.weight(1f))
            LIcon(R.drawable.lucide_ic_calendar, StaffInk, 22.dp)
        }
        VSpace(14.dp)
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFF1E3D79)).padding(20.dp)) {
            Txt("Ventas totales", 9, color = White.copy(alpha = 0.8f), family = Inter)
            VSpace(14.dp)
            Txt("$8.460.200", 26, color = White, family = Inter)
            VSpace(10.dp)
            Txt("18% sobre el mes anterior", 9, color = White.copy(alpha = 0.8f), family = Inter)
            VSpace(16.dp)
            Row(Modifier.fillMaxWidth().height(96.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                DemoData.reportBars.forEach { fraction ->
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(fraction)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(if (fraction == 1f) White else StaffChip),
                    )
                }
            }
        }
        VSpace(20.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ReportStat("126", "Atenciones", "92% asistió", Modifier.weight(1f))
            ReportStat("84", "Ventas", "$100.716 prom.", Modifier.weight(1f))
        }
        VSpace(4.dp)
        Row(
            Modifier.fillMaxWidth().height(48.dp).staffCard(radius = 16.dp, border = Lavender).tap {
                Toast.makeText(context, "Reporte exportado", Toast.LENGTH_SHORT).show()
            },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LIcon(R.drawable.lucide_ic_file_text, StaffInk, 18.dp)
            HSpace(10.dp)
            Txt("Exportar reporte", 13, weight = FontWeight.SemiBold, color = StaffInk, family = Inter)
        }
        VSpace(24.dp)
        Spacer(Modifier.height(1.dp).background(ScreenBg))
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
