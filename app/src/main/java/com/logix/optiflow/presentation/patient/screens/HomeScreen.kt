package com.logix.optiflow.presentation.patient.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.domain.model.inventory.FrameModel
import com.logix.optiflow.presentation.navigation.Routes
import com.logix.optiflow.presentation.patient.AppointmentUi
import com.logix.optiflow.presentation.patient.CatalogViewModel
import com.logix.optiflow.presentation.patient.components.DateBadge
import com.logix.optiflow.presentation.patient.components.PatientScreen
import com.logix.optiflow.presentation.patient.components.SectionHeader
import com.logix.optiflow.presentation.patient.components.StatusCapsule
import com.logix.optiflow.presentation.patient.components.image
import com.logix.optiflow.presentation.patient.components.patientViewModel
import com.logix.optiflow.presentation.patient.components.priceLabel
import com.logix.optiflow.presentation.patient.components.shapeLabel
import com.logix.optiflow.presentation.patient.components.tryOnStyle
import com.logix.optiflow.presentation.shared.components.BellButton
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.IconTile
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.PatientTab
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.card
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.theme.CardBorder
import com.logix.optiflow.presentation.theme.DeepNavy
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.Emerald50
import com.logix.optiflow.presentation.theme.Emerald600
import com.logix.optiflow.presentation.theme.Geist
import com.logix.optiflow.presentation.theme.HeaderTitle
import com.logix.optiflow.presentation.theme.Jakarta
import com.logix.optiflow.presentation.theme.NavyBlue
import com.logix.optiflow.presentation.theme.OpenSansCondensed
import com.logix.optiflow.presentation.theme.PatientHeader
import com.logix.optiflow.presentation.theme.Periwinkle
import com.logix.optiflow.presentation.theme.Poppins
import com.logix.optiflow.presentation.theme.SkyTint
import com.logix.optiflow.presentation.theme.Slate300
import com.logix.optiflow.presentation.theme.Slate500
import com.logix.optiflow.presentation.theme.Slate600
import com.logix.optiflow.presentation.theme.Teal700
import com.logix.optiflow.presentation.theme.WarmGray
import com.logix.optiflow.presentation.theme.WellnessBg
import com.logix.optiflow.presentation.theme.WellnessIcon
import com.logix.optiflow.presentation.theme.White

@Composable
fun PatientHomeScreen(nav: NavController, catalogVm: CatalogViewModel = hiltViewModel()) {
    val vm = patientViewModel()
    val state by vm.uiState.collectAsState()
    val catalog by catalogVm.uiState.collectAsStateWithLifecycle()
    val featured = catalog.frames.take(2)
    LaunchedEffect(Unit) { vm.refresh() }
    val context = LocalContext.current
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        // Desde Android 13 hay que pedir permiso para mostrar los push.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    PatientScreen(
        nav = nav,
        tab = PatientTab.HOME,
        topBar = {
            com.logix.optiflow.presentation.shared.components.StatusBarIcons(lightIcons = true)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(PatientHeader)
                    .statusBarsPadding()
                    .height(85.dp)
                    .padding(start = 19.dp, end = 19.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Txt(
                    "Hola, ${state.firstName}",
                    24,
                    weight = FontWeight.ExtraBold,
                    color = HeaderTitle,
                    letterSpacing = (-0.6).sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
                BellButton(onClick = { nav.navigate(Routes.P_NOTIFICATIONS) }, circle = false)
            }
        },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            TryOnBanner(onClick = {
                catalog.frames.firstOrNull { it.tryOnStyle != null }?.let { nav.navigate(Routes.tryOn(it.sku)) } ?: nav.navigate(Routes.P_CATALOG)
            })

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader("Próxima cita", "Ver todas", onAction = { nav.navigate(Routes.P_APPOINTMENTS) })
                val next = state.nextAppointment
                if (next != null) {
                    NextAppointmentCard(next) { nav.navigate(Routes.appointment(next.id)) }
                } else {
                    EmptyAppointmentCard { nav.navigate(Routes.book()) }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader("Accesos rápidos")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    QuickAccess(
                        icon = R.drawable.lucide_ic_file_text,
                        title = "Mis recetas",
                        modifier = Modifier.weight(1f),
                        onClick = { nav.navigate(Routes.P_PRESCRIPTION) },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(6.dp).clip(CircleShape).background(Electric))
                            HSpace(4.dp)
                            Txt(
                                when (val count = state.prescriptions.size) {
                                    0 -> "Sin recetas"
                                    1 -> "1 receta"
                                    else -> "$count recetas"
                                },
                                10,
                                weight = FontWeight.Medium,
                                color = Slate500,
                            )
                        }
                    }
                    QuickAccess(
                        icon = R.drawable.lucide_ic_package,
                        title = "Mis pedidos",
                        modifier = Modifier.weight(1.1f),
                        onClick = { nav.navigate(Routes.P_ORDERS) },
                    ) {
                        val active = state.orders.count { !it.delivered }
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Emerald50)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Txt("$active en camino", 10, weight = FontWeight.Bold, color = Emerald600)
                        }
                    }
                    QuickAccess(
                        icon = R.drawable.lucide_ic_heart,
                        title = "Historial",
                        modifier = Modifier.weight(1.1f),
                        onClick = { nav.navigate(Routes.P_HISTORY) },
                    ) {
                        Txt("Último control", 10, weight = FontWeight.Medium, color = Slate500)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Txt(
                        "Armazones Destacados",
                        18,
                        weight = FontWeight.SemiBold,
                        color = DeepNavy,
                        family = Poppins,
                        modifier = Modifier.weight(1f),
                    )
                    Txt(
                        "Ver todos",
                        14,
                        weight = FontWeight.Medium,
                        color = Electric,
                        family = Poppins,
                        modifier = Modifier.tap { nav.navigate(Routes.P_CATALOG) },
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    featured.forEach { frame ->
                        FeaturedFrame(frame, Modifier.weight(1f)) { nav.navigate(Routes.frame(frame.sku)) }
                    }
                    repeat(2 - featured.size) { Spacer(Modifier.weight(1f)) }
                }
            }

            WellnessWidget()
        }
    }
}

@Composable
private fun TryOnBanner(onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .tap(onClick),
    ) {
        Image(
            painter = painterResource(R.drawable.img_banner_ar),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
        Column(Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp)) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Teal700)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Txt("PRUEBA VIRTUAL", 11, weight = FontWeight.Bold, color = White, family = Geist)
            }
            VSpace(4.dp)
            Txt("Prueba tus lentes desde casa", 22, weight = FontWeight.Bold, color = White, family = Poppins, maxLines = 1)
            VSpace(26.dp)
            Txt(
                "Usa nuestra cámara AR para ver cómo te quedan",
                13,
                weight = FontWeight.Bold,
                color = White.copy(alpha = 0.9f),
                family = OpenSansCondensed,
            )
        }
        Row(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 19.dp)
                .height(30.dp)
                .card(radius = 12.dp, background = Periwinkle, border = Color(0xFF14336F), elevation = 4.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Txt("Pruebalo ahora", 13, weight = FontWeight.Bold, color = Electric, family = OpenSansCondensed)
            HSpace(8.dp)
            LIcon(R.drawable.lucide_ic_chevron_right, Electric, 12.dp)
        }
    }
}

@Composable
private fun NextAppointmentCard(appointment: AppointmentUi, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(99.dp)
            .card(radius = 24.dp, border = CardBorder.copy(alpha = 0.8f), elevation = 8.dp)
            .tap(onClick)
            .padding(17.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DateBadge(appointment.day, appointment.month)
        HSpace(14.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            StatusCapsule(appointment.status)
            Txt(
                if (appointment.fromApi) appointment.title else "Evaluación visual integral",
                14,
                weight = FontWeight.Bold,
                color = DeepNavy,
                maxLines = 1,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Txt(appointment.storeName, 12, weight = FontWeight.Medium, color = Slate500, maxLines = 1)
                Txt("•", 12, weight = FontWeight.Medium, color = Slate300)
                Txt("${appointment.time} hrs", 12, weight = FontWeight.SemiBold, color = NavyBlue)
            }
        }
        LIcon(R.drawable.lucide_ic_chevron_right, Slate500, 20.dp)
    }
}

@Composable
private fun EmptyAppointmentCard(onBook: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(99.dp)
            .card(radius = 24.dp, border = CardBorder.copy(alpha = 0.8f), elevation = 8.dp)
            .tap(onBook)
            .padding(17.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(R.drawable.lucide_ic_calendar_plus, Periwinkle, Electric, size = 56.dp, iconSize = 24.dp, radius = 16.dp)
        HSpace(14.dp)
        Column(Modifier.weight(1f)) {
            Txt("No tienes citas próximas", 14, weight = FontWeight.Bold, color = DeepNavy)
            Txt("Reserva tu evaluación visual", 12, weight = FontWeight.Medium, color = Slate500)
        }
        LIcon(R.drawable.lucide_ic_chevron_right, Slate500, 20.dp)
    }
}

@Composable
private fun QuickAccess(
    icon: Int,
    title: String,
    modifier: Modifier,
    onClick: () -> Unit,
    footer: @Composable () -> Unit,
) {
    Column(
        modifier
            .height(129.dp)
            .card(radius = 16.dp, border = CardBorder.copy(alpha = 0.7f), elevation = 1.dp)
            .tap(onClick)
            .padding(15.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        IconTile(icon, Periwinkle, NavyBlue)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Txt(title, 12, weight = FontWeight.Bold, color = DeepNavy, lineHeight = 15.sp)
            footer()
        }
    }
}

@Composable
private fun FeaturedFrame(frame: FrameModel, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .height(228.dp)
            .card(radius = 16.dp, border = CardBorder)
            .tap(onClick)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(frame.image),
            contentDescription = frame.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(12.dp)),
        )
        Column(Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Txt(frame.shapeLabel, 12, color = WarmGray, family = Geist)
            Txt(frame.name, 15, weight = FontWeight.Medium, color = DeepNavy, family = Poppins, maxLines = 1)
            Txt(frame.priceLabel, 14, color = Teal700, family = OpenSansCondensed)
        }
    }
}

@Composable
private fun WellnessWidget() {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WellnessBg)
            .border(1.dp, SkyTint, RoundedCornerShape(16.dp))
            .padding(17.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(R.drawable.lucide_ic_eye, WellnessIcon, DeepNavy, size = 44.dp, iconSize = 24.dp, radius = 16.dp)
        HSpace(14.dp)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Txt("Es hora de descansar la vista", 12, weight = FontWeight.Bold, color = DeepNavy)
            androidx.compose.material3.Text(
                buildAnnotatedString {
                    append("Aplica la regla ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = NavyBlue)) { append("20-20-20") }
                    append(": cada 20 minutos\nmira a 6 metros por 20 segundos.")
                },
                style = TextStyle(fontFamily = Jakarta, fontSize = 11.sp, lineHeight = 15.13.sp, color = Slate600),
            )
        }
    }
}
