package com.logix.optiflow.presentation.patient.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.presentation.navigation.Routes
import com.logix.optiflow.presentation.patient.OrderUi
import com.logix.optiflow.presentation.patient.PatientViewModel
import com.logix.optiflow.presentation.patient.components.PatientScreen
import com.logix.optiflow.presentation.patient.components.patientViewModel
import com.logix.optiflow.presentation.shared.components.Divider
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.IconTile
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.PatientDarkTopBar
import com.logix.optiflow.presentation.shared.components.PatientTab
import com.logix.optiflow.presentation.shared.components.Pill
import com.logix.optiflow.presentation.shared.components.SegmentChip
import com.logix.optiflow.presentation.shared.components.SolidButton
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.card
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.theme.DeepNavy
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.ErrorRed
import com.logix.optiflow.presentation.theme.Lavender
import com.logix.optiflow.presentation.theme.NavyBlue
import com.logix.optiflow.presentation.theme.Periwinkle
import com.logix.optiflow.presentation.theme.SkyTint
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.Slate500
import com.logix.optiflow.presentation.theme.White

@Composable
fun MyOrdersScreen(nav: NavController) {
    val vm = patientViewModel()
    val state by vm.uiState.collectAsState()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val orders = state.orders.filter { if (tab == 0) !it.delivered else it.delivered }

    PatientScreen(
        nav = nav,
        tab = PatientTab.ORDERS,
        topBar = {
            PatientDarkTopBar(
                "Mis Pedidos",
                onBack = { nav.popBackStack() },
                below = {
                    Row(
                        Modifier.padding(start = 24.dp, bottom = 22.dp),
                        horizontalArrangement = Arrangement.spacedBy(22.dp),
                    ) {
                        listOf("En curso", "Entregados").forEachIndexed { index, label ->
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
                .padding(horizontal = 24.dp),
        ) {
            VSpace(26.dp)
            if (orders.isNotEmpty()) {
                Txt(if (tab == 0) "Pedido activo" else "Pedidos entregados", 16, weight = FontWeight.Bold, color = DeepNavy)
                VSpace(12.dp)
                orders.forEach { order ->
                    OrderCard(order) { nav.navigate(Routes.order(order.id)) }
                    VSpace(14.dp)
                }
            }
            VSpace(if (orders.isEmpty()) 120.dp else 140.dp)
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                IconTile(R.drawable.lucide_ic_box, White, NavyBlue, size = 56.dp, iconSize = 22.dp, radius = 16.dp)
                VSpace(14.dp)
                Txt(
                    if (tab == 0) "No tienes más pedidos activos" else "No tienes más pedidos entregados",
                    14,
                    weight = FontWeight.Bold,
                    color = Color(0xFF111827),
                )
                VSpace(4.dp)
                Txt("Aquí verás el avance de tus próximos lentes.", 12, color = Slate400)
            }
        }
    }
}

@Composable
private fun OrderCard(order: OrderUi, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .card(radius = 16.dp, border = Lavender, elevation = 2.dp)
            .tap(onClick)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(R.drawable.lucide_ic_eye, Periwinkle, DeepNavy, size = 50.dp, iconSize = 22.dp, radius = 12.dp)
            HSpace(12.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Pill(
                    if (order.delivered) "Entregado" else order.statusLabel,
                    SkyTint.copy(alpha = 0.6f),
                    Electric,
                    icon = R.drawable.lucide_ic_package,
                    size = 11,
                    weight = FontWeight.SemiBold,
                )
                Txt(order.title, 15, weight = FontWeight.Bold, color = DeepNavy)
                Txt("Pedido #${order.code} · ${order.dateLabel}", 12, color = Slate500)
            }
            LIcon(R.drawable.lucide_ic_chevron_right, Slate500, 20.dp)
        }
        VSpace(14.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) { index ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(if (index < order.step.coerceAtMost(4)) Electric else Color(0xFFE6EBFD)),
                )
            }
        }
        VSpace(8.dp)
        Row {
            Txt("Pedido recibido", 11, color = DeepNavy, modifier = Modifier.weight(1f))
            Txt(order.statusLabel, 11, weight = FontWeight.Bold, color = DeepNavy)
        }
        VSpace(12.dp)
        Divider(Lavender)
        VSpace(12.dp)
        Row {
            Txt("Entrega estimada:  ", 12, color = Slate500)
            Txt(order.estimate.replace(" y ", "–"), 12, weight = FontWeight.SemiBold, color = DeepNavy)
        }
    }
}

@Composable
fun OrderTrackingScreen(nav: NavController, id: String) {
    val vm = patientViewModel()
    val state by vm.uiState.collectAsState()
    val order = state.orders.firstOrNull { it.id == id }
    if (order == null) {
        LaunchedEffect(id) { nav.popBackStack() }
        return
    }
    val steps =
        listOf(
            Triple("Pedido recibido", order.history.getOrNull(0) ?: "—", R.drawable.lucide_ic_check),
            // La orden nace al cerrar la venta, ya pagada.
            Triple("Pago confirmado", order.statusDates["PENDING"] ?: order.history.getOrNull(0) ?: "—", R.drawable.lucide_ic_check),
            Triple(
                "En producción",
                order.statusDates["IN_WORKSHOP"] ?: if (order.step >= 2) "Trabajando en tus lentes" else "Pendiente",
                R.drawable.lucide_ic_eye,
            ),
            Triple(
                if (order.delivered) "Entregado" else "Listo para retirar",
                order.statusDates[if (order.delivered) "DELIVERED" else "READY_FOR_DELIVERY"] ?: "Pendiente",
                R.drawable.lucide_ic_package,
            ),
        )

    PatientScreen(
        nav = nav,
        tab = PatientTab.ORDERS,
        topBar = { PatientDarkTopBar("Pedido #${order.code}", onBack = { nav.popBackStack() }) },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            VSpace(18.dp)
            Column(
                Modifier.fillMaxWidth().card(radius = 24.dp, elevation = 2.dp).padding(vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier.size(56.dp).shadow(3.dp, RoundedCornerShape(14.dp)).clip(RoundedCornerShape(14.dp)).background(White),
                    contentAlignment = Alignment.Center,
                ) {
                    LIcon(R.drawable.lucide_ic_eye, NavyBlue, 24.dp)
                }
                VSpace(10.dp)
                Pill(
                    if (order.delivered) "Entregado" else order.statusLabel,
                    White,
                    NavyBlue,
                    icon = R.drawable.lucide_ic_check,
                    border = SkyTint,
                    size = 12,
                    weight = FontWeight.SemiBold,
                )
                VSpace(14.dp)
                Txt(
                    when {
                        order.delivered -> "Tus lentes fueron entregados"
                        order.step >= 3 -> "Tus lentes están listos para retirar"
                        else -> "Tus lentes están en proceso"
                    },
                    18,
                    weight = FontWeight.Bold,
                    color = DeepNavy,
                )
                VSpace(6.dp)
                Txt(
                    if (order.delivered) "Gracias por confiar en nosotros." else "Entrega estimada: ${order.estimate}.",
                    13,
                    color = Slate500,
                )
                order.delayReason?.let {
                    VSpace(6.dp)
                    Txt("Tu pedido tiene un retraso: $it", 13, color = ErrorRed)
                }
            }
            VSpace(22.dp)
            Txt("Seguimiento", 16, weight = FontWeight.Bold, color = DeepNavy)
            VSpace(14.dp)
            steps.forEachIndexed { index, (title, subtitle, icon) ->
                val reached = index < order.step || order.delivered
                val current = index == order.step && !order.delivered
                Row {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (reached || current) NavyBlue else Color(0xFFF1F5F9))
                                .border(1.dp, if (reached || current) NavyBlue else Color(0xFFE2E8F0), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            LIcon(
                                if (reached) R.drawable.lucide_ic_check else icon,
                                if (reached || current) White else Slate400,
                                14.dp,
                            )
                        }
                        if (index < steps.lastIndex) {
                            Box(
                                Modifier
                                    .width(2.dp)
                                    .height(27.dp)
                                    .background(if (index < order.step) NavyBlue else Color(0xFFE2E8F0)),
                            )
                        }
                    }
                    HSpace(14.dp)
                    Column(Modifier.padding(top = 2.dp)) {
                        Txt(title, 14, weight = FontWeight.Bold, color = DeepNavy)
                        Txt(subtitle, 11, color = Slate400)
                    }
                }
            }
            VSpace(16.dp)
            Column(Modifier.fillMaxWidth().card(radius = 16.dp, elevation = 2.dp).padding(horizontal = 16.dp, vertical = 14.dp)) {
                InfoLine(R.drawable.lucide_ic_eye, "PRODUCTO", order.product, order.productDetail)
                VSpace(12.dp)
                Divider(Lavender)
                VSpace(12.dp)
                InfoLine(R.drawable.lucide_ic_map_pin, "RETIRO EN", order.pickup, null)
            }
            VSpace(22.dp)
            SolidButton(
                "¿Necesitas ayuda?",
                onClick = { nav.navigate(Routes.P_SETTINGS) },
                background = White,
                contentColor = NavyBlue,
                border = SkyTint,
                height = 52.dp,
                radius = 16.dp,
                fontSize = 15,
                leadingIcon = R.drawable.lucide_ic_circle_question_mark,
            )
            VSpace(18.dp)
        }
    }
}

@Composable
private fun InfoLine(icon: Int, label: String, value: String, detail: String?) {
    Row {
        LIcon(icon, NavyBlue, 20.dp, Modifier.padding(top = 4.dp))
        HSpace(14.dp)
        Column {
            Txt(label, 10, weight = FontWeight.SemiBold, color = Slate400, letterSpacing = 0.5.sp)
            Txt(value, 14, weight = FontWeight.Bold, color = DeepNavy)
            if (detail != null) {
                VSpace(4.dp)
                Txt(detail, 12, color = Slate500, align = TextAlign.Start)
            }
        }
    }
}
