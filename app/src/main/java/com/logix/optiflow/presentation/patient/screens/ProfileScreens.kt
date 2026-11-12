package com.logix.optiflow.presentation.patient.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.presentation.navigation.Routes
import com.logix.optiflow.presentation.navigation.switchRoot
import com.logix.optiflow.presentation.patient.PatientAccountViewModel
import com.logix.optiflow.presentation.patient.SettingsViewModel
import com.logix.optiflow.presentation.patient.components.PatientScreen
import com.logix.optiflow.presentation.patient.components.patientViewModel
import com.logix.optiflow.presentation.shared.components.Divider
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.IconTile
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.PatientLightTopBar
import com.logix.optiflow.presentation.shared.components.PatientTab
import com.logix.optiflow.presentation.shared.components.SolidButton
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.card
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.theme.DeepNavy
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.Lavender
import com.logix.optiflow.presentation.theme.NavyBlue
import com.logix.optiflow.presentation.theme.PatientHeader
import com.logix.optiflow.presentation.theme.Periwinkle
import com.logix.optiflow.presentation.theme.SkyTint
import com.logix.optiflow.presentation.theme.Slate300
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.Slate500
import com.logix.optiflow.presentation.theme.White

@Composable
fun ProfileScreen(nav: NavController, accountVm: PatientAccountViewModel = hiltViewModel()) {
    val vm = patientViewModel()
    val state by vm.uiState.collectAsState()
    val account by accountVm.uiState.collectAsState()

    PatientScreen(
        nav = nav,
        tab = PatientTab.PROFILE,
        topBar = {
            PatientLightTopBar("Mi Perfil", onBack = { nav.popBackStack() }) {
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFE4E9FB)).tap {
                        nav.navigate(Routes.P_SETTINGS)
                    },
                    contentAlignment = Alignment.Center,
                ) {
                    LIcon(R.drawable.lucide_ic_settings, PatientHeader, 20.dp)
                }
            }
        },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(84.dp).clip(RoundedCornerShape(24.dp)).background(Lavender),
                    contentAlignment = Alignment.Center,
                ) {
                    Txt(state.initials, 24, weight = FontWeight.Bold, color = Electric)
                }
                VSpace(18.dp)
                Txt(state.displayName, 22, weight = FontWeight.Bold, color = DeepNavy)
                VSpace(4.dp)
                Txt(state.email, 13, color = Slate500)
                VSpace(14.dp)
                Row(
                    Modifier
                        .height(34.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(White)
                        .tap { nav.navigate(Routes.P_EDIT_PROFILE) }
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LIcon(R.drawable.lucide_ic_pencil, DeepNavy, 14.dp)
                    HSpace(8.dp)
                    Txt("Editar perfil", 13, weight = FontWeight.SemiBold, color = DeepNavy)
                }
            }
            VSpace(22.dp)
            PointsCard(account.points)
            VSpace(22.dp)
            Txt("Mi salud visual", 15, weight = FontWeight.SemiBold, color = DeepNavy)
            VSpace(10.dp)
            MenuGroup {
                MenuRow(R.drawable.lucide_ic_heart, "Historial clínico", "Controles y antecedentes") { nav.navigate(Routes.P_HISTORY) }
                Divider(Lavender)
                MenuRow(R.drawable.lucide_ic_file_text, "Mis recetas", prescriptionsLabel(state.prescriptions.size)) {
                    nav.navigate(Routes.P_PRESCRIPTIONS)
                }
            }
            VSpace(18.dp)
            Txt("Cuenta", 15, weight = FontWeight.SemiBold, color = DeepNavy, modifier = Modifier.padding(start = 4.dp))
            VSpace(10.dp)
            MenuGroup {
                MenuRow(R.drawable.lucide_ic_receipt, "Mis compras", purchasesLabel(account.purchases.size, account.isLoading)) {
                    nav.navigate(Routes.P_PURCHASES)
                }
                Divider(Lavender)
                MenuRow(R.drawable.lucide_ic_settings, "Configuración", null) { nav.navigate(Routes.P_SETTINGS) }
                Divider(Lavender)
                MenuRow(R.drawable.lucide_ic_circle_question_mark, "Ayuda y soporte", null) { nav.navigate(Routes.info(InfoTopic.HELP.name)) }
            }
            VSpace(18.dp)
            SolidButton(
                "Cerrar sesión",
                onClick = { vm.clearSession { nav.switchRoot(Routes.AUTH) } },
                background = White,
                contentColor = Electric,
                border = SkyTint,
                height = 46.dp,
                radius = 12.dp,
                fontSize = 15,
            )
            VSpace(14.dp)
            Txt(
                "Lo último que consultaste se guarda en este dispositivo para verlo sin conexión.",
                11,
                color = Slate500,
                align = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            VSpace(16.dp)
        }
    }
}

@Composable
private fun PointsCard(points: Int?) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PatientHeader)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(White.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
            LIcon(R.drawable.lucide_ic_star, White, 20.dp)
        }
        HSpace(14.dp)
        Column(Modifier.weight(1f)) {
            Txt("Mis puntos OptiFlow", 13, color = White.copy(alpha = 0.8f))
            Txt(points?.let { "$it puntos" } ?: "Consultando...", 20, weight = FontWeight.Bold, color = White)
        }
        Txt("+100 en tu\ncumpleaños", 11, color = White.copy(alpha = 0.8f), align = TextAlign.End)
    }
}

private fun prescriptionsLabel(count: Int): String =
    when (count) {
        0 -> "Aún no tienes recetas"
        1 -> "1 receta disponible"
        else -> "$count recetas disponibles"
    }

private fun purchasesLabel(count: Int, loading: Boolean): String =
    when {
        loading -> "Consultando..."
        count == 0 -> "Aún no tienes compras"
        count == 1 -> "1 compra"
        else -> "$count compras"
    }

@Composable
fun MenuGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().card(radius = 16.dp, border = Lavender, elevation = 2.dp).padding(horizontal = 16.dp),
        content = content,
    )
}

@Composable
fun MenuRow(icon: Int, title: String, subtitle: String?, trailing: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().tap(onClick).padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(icon, Periwinkle, NavyBlue, size = 40.dp, iconSize = 20.dp, radius = 12.dp)
        HSpace(16.dp)
        Column(Modifier.weight(1f)) {
            Txt(title, 14, weight = FontWeight.Bold, color = DeepNavy)
            if (subtitle != null) Txt(subtitle, 12, color = Slate500)
        }
        if (trailing != null) trailing() else LIcon(R.drawable.lucide_ic_chevron_right, Slate500, 20.dp)
    }
}

@Composable
fun ClinicalHistoryScreen(nav: NavController) {
    val vm = patientViewModel()
    val state by vm.uiState.collectAsState()
    val latest = state.history.firstOrNull()

    PatientScreen(
        nav = nav,
        tab = PatientTab.PROFILE,
        topBar = { PatientLightTopBar("Historial clínico", onBack = { nav.popBackStack() }) },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            VSpace(8.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .card(radius = 20.dp, background = NavyBlue, elevation = 6.dp)
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconTile(R.drawable.lucide_ic_eye, White.copy(alpha = 0.15f), White, size = 48.dp, iconSize = 22.dp, radius = 12.dp)
                HSpace(16.dp)
                Column {
                    Txt("Último control", 12, color = White.copy(alpha = 0.75f))
                    Txt(
                        latest?.let { prettyDate(it.date) } ?: "Sin controles registrados",
                        18,
                        weight = FontWeight.Bold,
                        color = White,
                    )
                    if (latest != null) Txt("Próximo control recomendado en 9 meses", 12, color = White.copy(alpha = 0.8f))
                }
            }
            VSpace(28.dp)
            Txt("Atenciones", 18, weight = FontWeight.Bold, color = DeepNavy)
            VSpace(16.dp)
            if (state.history.isEmpty()) {
                Txt("Aún no tienes atenciones registradas.", 14, color = Slate500)
            }
            state.history.forEachIndexed { index, entry ->
                Row(Modifier.tap { nav.navigate(Routes.P_PRESCRIPTION) }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(16.dp)) {
                        Box(
                            Modifier
                                .padding(top = 2.dp)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(White)
                                .border(2.dp, White, CircleShape)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(Electric),
                        )
                        Box(Modifier.width(2.dp).height(if (index == state.history.lastIndex) 84.dp else 118.dp).background(Lavender))
                    }
                    HSpace(12.dp)
                    Column(Modifier.weight(1f)) {
                        Txt(entry.date, 12, weight = FontWeight.Bold, color = Electric, letterSpacing = 0.5.sp)
                        VSpace(6.dp)
                        Txt(entry.title, 17, weight = FontWeight.Bold, color = DeepNavy)
                        VSpace(6.dp)
                        Txt(entry.summary, 14, color = Slate500)
                        VSpace(14.dp)
                        Txt(entry.professional, 12, color = Slate400)
                    }
                    LIcon(R.drawable.lucide_ic_chevron_right, DeepNavy, 20.dp)
                }
            }
        }
    }
}

private fun prettyDate(upper: String): String {
    val months =
        mapOf(
            "ENE" to "enero", "FEB" to "febrero", "MAR" to "marzo", "ABR" to "abril", "MAY" to "mayo", "JUN" to "junio",
            "JUL" to "julio", "AGO" to "agosto", "SEP" to "septiembre", "SET" to "septiembre", "OCT" to "octubre",
            "NOV" to "noviembre", "DIC" to "diciembre",
        )
    val parts = upper.split(" ")
    if (parts.size != 3) return upper
    return "${parts[0].trimStart('0')} de ${months[parts[1]] ?: parts[1].lowercase()} de ${parts[2]}"
}

@Composable
fun SettingsScreen(nav: NavController, staff: Boolean = false, settingsVm: SettingsViewModel = hiltViewModel()) {
    val reminders by settingsVm.checkupReminders.collectAsState()

    val content: @Composable ColumnScope.() -> Unit = {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Txt("Preferencias", 15, weight = FontWeight.Bold, color = DeepNavy, modifier = Modifier.padding(start = 4.dp))
            VSpace(10.dp)
            MenuGroup {
                MenuRow(R.drawable.lucide_ic_bell, "Notificaciones", "Citas, pedidos y recordatorios") {
                    if (!staff) nav.navigate(Routes.P_NOTIFICATIONS) else nav.navigate(Routes.S_ALERTS)
                }
                if (!staff) {
                    Divider(Lavender)
                    Row(Modifier.fillMaxWidth().tap(settingsVm::toggleCheckupReminders).padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Txt("Recordatorios de control", 14, weight = FontWeight.Bold, color = DeepNavy)
                            Txt("Avisos preventivos de salud visual", 12, color = Slate500)
                        }
                        Toggle(reminders)
                    }
                }
                Divider(Lavender)
                MenuRow(R.drawable.lucide_ic_lock, "Privacidad y seguridad", null) { nav.navigate(Routes.info(InfoTopic.PRIVACY.name, staff)) }
                Divider(Lavender)
                MenuRow(R.drawable.lucide_ic_circle_question_mark, "Ayuda y documentación", null) { nav.navigate(Routes.info(InfoTopic.HELP.name, staff)) }
            }
            VSpace(22.dp)
            Txt("Aplicación", 15, weight = FontWeight.Bold, color = DeepNavy, modifier = Modifier.padding(start = 4.dp))
            VSpace(10.dp)
            MenuGroup {
                // La app solo está en español: se muestra como dato, sin flecha.
                MenuRow(R.drawable.lucide_ic_settings, "Idioma", "Español", trailing = {}) {}
                Divider(Lavender)
                MenuRow(R.drawable.lucide_ic_file_text, "Términos y privacidad", null) { nav.navigate(Routes.info(InfoTopic.TERMS.name, staff)) }
            }
            VSpace(36.dp)
            Txt(
                "OptiFlow · Versión 1.0.0",
                12,
                color = Slate300,
                align = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (staff) {
        com.logix.optiflow.presentation.shared.components.ScreenFrame(
            topBar = { PatientLightTopBar("Configuración", onBack = { nav.popBackStack() }) },
            content = content,
        )
    } else {
        PatientScreen(
            nav = nav,
            tab = PatientTab.PROFILE,
            topBar = { PatientLightTopBar("Configuración", onBack = { nav.popBackStack() }) },
            content = content,
        )
    }
}

@Composable
private fun Toggle(checked: Boolean) {
    Box(
        Modifier
            .size(width = 48.dp, height = 28.dp)
            .clip(CircleShape)
            .background(if (checked) PatientHeader else Color(0xFFE4E9FB))
            .padding(3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(Modifier.size(22.dp).shadow(2.dp, CircleShape).clip(CircleShape).background(White))
    }
}
