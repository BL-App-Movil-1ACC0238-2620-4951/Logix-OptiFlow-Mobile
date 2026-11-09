package com.logix.optiflow.presentation.staff.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.data.demo.DemoData
import com.logix.optiflow.data.demo.ProductionStage
import com.logix.optiflow.presentation.navigation.Routes
import com.logix.optiflow.presentation.shared.components.Divider
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.StaffBackTopBar
import com.logix.optiflow.presentation.shared.components.StaffCircleAction
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.tap
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
import com.logix.optiflow.presentation.theme.Inter
import com.logix.optiflow.presentation.theme.Lavender
import com.logix.optiflow.presentation.theme.Slate200
import com.logix.optiflow.presentation.theme.Slate300
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.StaffAccent
import com.logix.optiflow.presentation.theme.StaffBlue50
import com.logix.optiflow.presentation.theme.StaffBorder
import com.logix.optiflow.presentation.theme.StaffChip
import com.logix.optiflow.presentation.theme.StaffHeader
import com.logix.optiflow.presentation.theme.StaffInk
import com.logix.optiflow.presentation.theme.White

private val AccentLight = Color(0xFF60A5FA)

@Composable
fun NewPatientScreen(nav: NavController) {
    val vm = staffViewModel()
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var rut by rememberSaveable { mutableStateOf("") }
    var reason by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Nuevo Cliente", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(16.dp)
        StaffHero(padding = PaddingValues(20.dp, 22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StaffIconTile(R.drawable.lucide_ic_file_text, size = 36.dp, background = White, radius = 10.dp)
                HSpace(14.dp)
                Column {
                    Txt("FICHA DIGITAL", 9, color = AccentLight, family = Inter, letterSpacing = 1.sp)
                    VSpace(6.dp)
                    Txt("Registro rápido de paciente", 18, color = White, family = Inter)
                    VSpace(6.dp)
                    Txt("Ingresa los datos indispensables para abrir su ficha clínica.", 9, color = White.copy(alpha = 0.85f), family = Inter)
                }
            }
        }
        VSpace(18.dp)
        StaffLabel("Nombre completo")
        VSpace(10.dp)
        StaffInput(name, { name = it }, "Camila Paz Morales", R.drawable.lucide_ic_user)
        VSpace(20.dp)
        StaffLabel("Teléfono móvil")
        VSpace(10.dp)
        StaffInput(phone, { phone = it }, "+56 9 8745 9210", R.drawable.lucide_ic_phone, KeyboardType.Phone)
        VSpace(20.dp)
        StaffLabel("RUT / identificación")
        VSpace(10.dp)
        StaffInput(rut, { rut = it }, "18.942.105-K", R.drawable.lucide_ic_credit_card)
        VSpace(28.dp)
        Txt("Motivo de consulta", 16, weight = FontWeight.Bold, color = StaffInk, family = Inter)
        VSpace(12.dp)
        val reasons =
            listOf(
                R.drawable.lucide_ic_eye to "Primera consulta",
                R.drawable.lucide_ic_calendar to "Control anual",
                R.drawable.lucide_ic_layout_grid to "Cambio de cristales",
                R.drawable.lucide_ic_circle_question_mark to "Molestia visual",
            )
        reasons.chunked(2).forEachIndexed { row, chunk ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                chunk.forEachIndexed { col, (icon, label) ->
                    val index = row * 2 + col
                    val selected = reason == index
                    Row(
                        Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) StaffAccent else StaffSoft)
                            .tap { reason = index },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LIcon(icon, if (selected) White else StaffInk, 16.dp)
                        HSpace(8.dp)
                        Txt(label, 11, color = if (selected) White else StaffInk, family = Inter)
                    }
                }
            }
            VSpace(8.dp)
        }
        VSpace(14.dp)
        StaffButton("Guardar paciente", onClick = {
            if (name.isBlank()) {
                Toast.makeText(context, "Ingresa el nombre del paciente", Toast.LENGTH_SHORT).show()
            } else {
                val patient = vm.addPatient(name, phone, rut)
                Toast.makeText(context, "Paciente registrado", Toast.LENGTH_SHORT).show()
                nav.navigate(Routes.staffPatient(patient.id)) { popUpTo(Routes.S_PATIENTS) }
            }
        }, icon = R.drawable.lucide_ic_check)
        VSpace(14.dp)
        Txt(
            "Cancelar",
            13,
            weight = FontWeight.Bold,
            color = StaffAccent,
            family = Inter,
            modifier = Modifier.align(Alignment.CenterHorizontally).tap { nav.popBackStack() },
        )
        VSpace(24.dp)
    }
}

@Composable
fun NewAppointmentScreen(nav: NavController) {
    var time by rememberSaveable { mutableIntStateOf(1) }
    val context = LocalContext.current
    val times = listOf("09:00", "10:30", "12:00", "14:30", "16:00", "17:00")

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Nueva cita", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(16.dp)
        StaffHero(radius = 20.dp, padding = PaddingValues(18.dp, 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StaffIconTile(R.drawable.lucide_ic_calendar, size = 36.dp, background = White, radius = 10.dp)
                HSpace(14.dp)
                Column {
                    Txt("AGENDAMIENTO · PASO ÚNICO", 9, color = AccentLight, family = Inter, letterSpacing = 1.sp)
                    VSpace(4.dp)
                    Txt("Datos de la cita", 18, color = White, family = Inter)
                }
            }
        }
        VSpace(18.dp)
        StaffLabel("Nombre del paciente", "Paciente registrado")
        VSpace(8.dp)
        StaffSelect("Valentina Silva Morales", null, onClick = { nav.navigate(Routes.S_PATIENTS) }, leading = {
            StaffInitials("VS", size = 28.dp, fontSize = 9)
        })
        VSpace(18.dp)
        StaffLabel("Servicio", "45 min aprox.")
        VSpace(8.dp)
        StaffSelect("Examen de vista completo", R.drawable.lucide_ic_eye, onClick = {})
        VSpace(18.dp)
        StaffLabel("Óptica / sucursal", "Disponible")
        VSpace(8.dp)
        StaffSelect("Sucursal Centro · Av. Libertad 450", R.drawable.lucide_ic_map_pin, onClick = {})
        VSpace(18.dp)
        StaffLabel("Fecha", "Junio 2025")
        VSpace(8.dp)
        StaffSelect("Miércoles, 20 de junio", R.drawable.lucide_ic_calendar, onClick = {})
        VSpace(22.dp)
        StaffSectionTitle("Hora", "Selecciona horario")
        VSpace(12.dp)
        times.chunked(3).forEachIndexed { row, chunk ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                chunk.forEachIndexed { col, label ->
                    val index = row * 3 + col
                    val selected = index == time
                    Box(
                        Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) StaffAccent else White)
                            .border(1.dp, if (selected) StaffAccent else StaffBorder, RoundedCornerShape(10.dp))
                            .tap { time = index },
                        contentAlignment = Alignment.Center,
                    ) {
                        Txt(label, 14, color = if (selected) White else StaffInk, family = Inter)
                    }
                }
            }
            VSpace(10.dp)
        }
        VSpace(14.dp)
        StaffButton("Confirmar cita", onClick = {
            Toast.makeText(context, "Cita agendada para las ${times[time]}", Toast.LENGTH_SHORT).show()
            nav.navigate(Routes.S_AGENDA) { popUpTo(Routes.S_HOME) }
        }, icon = R.drawable.lucide_ic_check)
        VSpace(24.dp)
    }
}

private data class Stage(val icon: Int, val title: String, val subtitle: String)

private val stages =
    listOf(
        Stage(R.drawable.lucide_ic_check, "En sala de espera", "Llegada registrada a las 10:18"),
        Stage(R.drawable.lucide_ic_briefcase, "En consulta optométrica", "Anamnesis y biomicroscopía"),
        Stage(R.drawable.lucide_ic_eye, "Pruebas de graduación", "Refracción y test cromático"),
        Stage(R.drawable.lucide_ic_layout_grid, "Elección de armazón y cristales", "Asesoría y medidas pupilares"),
        Stage(R.drawable.lucide_ic_settings, "Derivado a taller / laboratorio", "Biselado, corte y montaje"),
        Stage(R.drawable.lucide_ic_file_text, "Atención finalizada", "Receta y comprobante clínico"),
    )

@Composable
fun ClinicalCareScreen(nav: NavController) {
    val vm = staffViewModel()
    val state by vm.uiState.collectAsState()
    var note by rememberSaveable { mutableStateOf("") }
    val current = state.clinicalStage

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Atención clínica", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(16.dp)
        StaffHero(radius = 20.dp, padding = PaddingValues(16.dp, 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LIcon(R.drawable.lucide_ic_clock, White, 14.dp)
                HSpace(6.dp)
                Txt("Atención en curso", 10, weight = FontWeight.SemiBold, color = White, family = Inter, modifier = Modifier.weight(1f))
                Txt("Box 3 · Dra. Ramos", 9, color = Slate300, family = Inter)
            }
            VSpace(16.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                StaffInitials("VS", size = 40.dp)
                HSpace(12.dp)
                Column {
                    Txt("Valentina Silva Morales", 14, color = White, family = Inter)
                    Txt("Ficha #OPT-2024-849 · 10:30", 9, color = Slate300, family = Inter)
                }
            }
            VSpace(16.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                LIcon(R.drawable.lucide_ic_eye, White, 14.dp)
                HSpace(8.dp)
                Txt("Evaluación visual completa · Turno #04", 9, color = White, family = Inter)
            }
        }
        VSpace(20.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Txt("Actualizar estado de la consulta", 16, weight = FontWeight.Bold, color = StaffInk, family = Inter, modifier = Modifier.weight(1f))
            Txt("Paso ${current + 1} de 6", 11, weight = FontWeight.SemiBold, color = StaffAccent, family = Inter)
        }
        VSpace(10.dp)
        Txt("Selecciona la etapa actual para sincronizar recepción y taller.", 12, color = StaffAccent, family = Inter)
        VSpace(12.dp)
        stages.forEachIndexed { index, stage ->
            val done = index < current
            val active = index == current
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (active) Color(0xFFF0F4FF) else White)
                    .border(if (active) 1.5.dp else 0.dp, if (active) StaffAccent else Color.Transparent, RoundedCornerShape(14.dp))
                    .tap { vm.setClinicalStage(index) }
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StaffIconTile(
                    stage.icon,
                    size = 34.dp,
                    background = if (active) Color(0xFF0013C1) else Color(0xFFF4F5FF),
                    tint = if (active) White else StaffAccent,
                    radius = 10.dp,
                )
                HSpace(12.dp)
                Column(Modifier.weight(1f)) {
                    Txt(
                        stage.title,
                        14,
                        color = StaffInk,
                        family = Inter,
                        decoration = if (done) TextDecoration.LineThrough else null,
                    )
                    Txt(stage.subtitle, 10, color = StaffAccent, family = Inter)
                }
                if (done) LIcon(R.drawable.lucide_ic_check, StaffInk, 16.dp)
                if (active) {
                    LIcon(R.drawable.lucide_ic_check, StaffAccent, 12.dp)
                    HSpace(4.dp)
                    Txt("En proceso", 10, weight = FontWeight.SemiBold, color = StaffAccent, family = Inter)
                }
            }
            VSpace(8.dp)
        }
        VSpace(12.dp)
        StaffLabel("Nota de transición", "Opcional")
        VSpace(8.dp)
        StaffInput(note, { note = it }, "Añade una observación para la siguiente etapa...", height = 86.dp, singleLine = false)
        VSpace(20.dp)
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(StaffSoft).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StaffIconTile(R.drawable.lucide_ic_bell, size = 34.dp, background = Lavender, tint = StaffInk, radius = 10.dp)
            HSpace(12.dp)
            Column {
                Txt("Sincronización en tiempo real", 11, color = StaffInk, family = Inter)
                Txt("El cambio se notificará a recepción y a la pantalla de sala.", 9, color = StaffAccent, family = Inter)
            }
        }
        VSpace(22.dp)
        StaffButton("Guardar y continuar atención", onClick = {
            vm.setClinicalStage((current + 1).coerceAtMost(stages.lastIndex))
            nav.navigate(Routes.S_EVALUATION)
        }, icon = R.drawable.lucide_ic_check)
        VSpace(24.dp)
    }
}

@Composable
fun StaffPatientRecordScreen(nav: NavController, id: String) {
    val vm = staffViewModel()
    val state by vm.uiState.collectAsState()
    val patient = state.patients.firstOrNull { it.id == id }
    if (patient == null) {
        LaunchedEffect(id) { nav.popBackStack() }
        return
    }

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = {
            StaffBackTopBar("Ficha del Paciente", onBack = { nav.popBackStack() }) {
                StaffCircleAction(R.drawable.lucide_ic_ellipsis) {}
            }
        },
    ) {
        VSpace(16.dp)
        StaffHero(radius = 20.dp, padding = PaddingValues(16.dp, 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StaffInitials(patient.initials, size = 64.dp, fontSize = 16)
                HSpace(14.dp)
                Column {
                    StaffPill("Paciente activo", background = Color(0x3310B981), color = Color(0xFFA7F3D0))
                    VSpace(4.dp)
                    Txt(patient.fullName, 20, weight = FontWeight.Bold, color = White, family = Inter, letterSpacing = (-0.6).sp)
                    Txt("ID #${patient.ficha}", 10, color = Slate300, family = Inter)
                }
            }
            VSpace(22.dp)
            Row {
                Column(Modifier.weight(1f)) {
                    Txt("TELÉFONO", 8, color = Slate300, family = Inter)
                    VSpace(6.dp)
                    Txt(patient.phone, 15, color = White, family = Inter)
                }
                Column(Modifier.weight(1f)) {
                    Txt("ÚLTIMA VISITA", 8, color = Slate300, family = Inter)
                    VSpace(6.dp)
                    Txt(patient.lastVisit, 15, color = White, family = Inter)
                }
            }
            VSpace(18.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StaffButton("Llamar", {}, R.drawable.lucide_ic_phone, background = White, contentColor = Color(0xFF0013C1), height = 46.dp, modifier = Modifier.weight(1f))
                StaffButton("Mensaje", {}, R.drawable.lucide_ic_mail, background = White, contentColor = Color(0xFF0013C1), height = 46.dp, modifier = Modifier.weight(1f))
            }
        }
        VSpace(24.dp)
        StaffSectionTitle("Última prescripción", "Ver historial") { nav.navigate(Routes.S_EVALUATION) }
        VSpace(12.dp)
        Column(Modifier.fillMaxWidth().staffCard().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LIcon(R.drawable.lucide_ic_eye, StaffAccent, 26.dp)
                HSpace(14.dp)
                Column(Modifier.weight(1f)) {
                    Txt("Última Prescripción", 18, color = StaffInk, family = Inter)
                    Txt("Dr. Sergio Ramos · 24 jun 2023", 8, color = StaffInk, family = Inter)
                }
                StaffPill("Vigente")
            }
            VSpace(14.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EyeBox("Ojo derecho", "-1.75 / -0.50", "180° · DP 31.5", Modifier.weight(1f))
                EyeBox("Ojo izquierdo", "-2.00 / -0.75", "175° · DP 31.0", Modifier.weight(1f))
            }
            VSpace(8.dp)
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF2FF)).padding(12.dp)) {
                Txt("Monofocal Filtro Azul 1.60", 13, color = StaffAccent, family = Inter)
            }
        }
        VSpace(24.dp)
        StaffSectionTitle("Próxima cita")
        VSpace(12.dp)
        Row(
            Modifier.fillMaxWidth().staffCard().tap { nav.navigate(Routes.S_AGENDA) }.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                Modifier.size(56.dp, 60.dp).clip(RoundedCornerShape(12.dp)).background(StaffChip),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Txt("20", 20, color = StaffInk, family = Inter)
                Txt("JUN", 11, color = StaffInk, family = Inter)
            }
            HSpace(12.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StaffPill("Confirmada")
                Txt("Evaluación visual completa", 13, color = StaffInk, family = Inter)
                Txt("10:30 · Control visual integral", 10, color = Slate400, family = Inter)
            }
            LIcon(R.drawable.lucide_ic_chevron_right, StaffInk, 20.dp)
        }
        VSpace(24.dp)
        StaffSectionTitle("Proceso de sus lentes", "Orden OT-2048") { nav.navigate(Routes.S_PRODUCTION) }
        VSpace(12.dp)
        Column(Modifier.fillMaxWidth().staffCard().tap { nav.navigate(Routes.S_PRODUCTION) }.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StaffIconTile(R.drawable.lucide_ic_settings, size = 36.dp, background = StaffSoft)
                HSpace(12.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    StaffPill("En taller", icon = R.drawable.lucide_ic_clock)
                    Txt("Biselado y montaje", 13, color = StaffInk, family = Inter)
                    Txt("Montura Ray-Ban · Cristales monofocales 1.60", 9, color = StaffInk, family = Inter)
                }
                LIcon(R.drawable.lucide_ic_chevron_right, StaffInk, 20.dp)
            }
            VSpace(14.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(StaffAccent, StaffAccent, AccentLight, StaffChip).forEach {
                    Box(Modifier.weight(1f).height(5.dp).clip(CircleShape).background(it))
                }
            }
            VSpace(8.dp)
            Row {
                Txt("Pedido recibido", 8, color = StaffInk, family = Inter, modifier = Modifier.weight(1f))
                Txt("Entrega estimada: 28 de junio", 8, color = StaffAccent, family = Inter)
            }
        }
        VSpace(18.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StaffButton("Nueva cita", { nav.navigate(Routes.S_NEW_APPOINTMENT) }, R.drawable.lucide_ic_calendar, modifier = Modifier.weight(1f))
            StaffButton(
                "Iniciar atención",
                { nav.navigate(Routes.S_CLINICAL) },
                R.drawable.lucide_ic_plus,
                background = White,
                contentColor = StaffInk,
                border = StaffBorder,
                modifier = Modifier.weight(1f),
            )
        }
        VSpace(24.dp)
    }
}

@Composable
private fun EyeBox(label: String, value: String, detail: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF2FF)).padding(12.dp)) {
        Txt(label, 8, color = StaffInk, family = Inter)
        VSpace(6.dp)
        Txt(value, 17, color = StaffInk, family = Inter)
        VSpace(4.dp)
        Txt(detail, 8, color = StaffAccent, family = Inter)
    }
}

@Composable
fun VisualEvaluationScreen(nav: NavController) {
    val context = LocalContext.current
    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = {
            StaffBackTopBar("Evaluación Visual", onBack = { nav.popBackStack() }, eyebrow = "Óptica Visión Norte") {
                StaffCircleAction(R.drawable.lucide_ic_user) { nav.navigate(Routes.staffPatient("vs")) }
            }
        },
    ) {
        VSpace(16.dp)
        StaffHero(background = Color(0xFF172A46), radius = 20.dp, padding = PaddingValues(16.dp, 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StaffInitials("VS", size = 40.dp)
                HSpace(12.dp)
                Column(Modifier.weight(1f)) {
                    Txt("Valentina Silva", 14, color = White, family = Inter)
                    Txt("Ficha #OPT-2024-849 · Control anual", 9, color = Slate300, family = Inter)
                }
                StaffPill("28 años", background = White, color = StaffAccent)
            }
            VSpace(26.dp)
            Row {
                Column(Modifier.weight(1f)) {
                    Txt("FECHA", 9, color = Slate300, family = Inter)
                    VSpace(6.dp)
                    Txt("20 Jun 2024", 14, color = White, family = Inter)
                }
                Column(Modifier.weight(1f)) {
                    Txt("OPTOMETRISTA", 9, color = Slate300, family = Inter)
                    VSpace(6.dp)
                    Txt("Dr. C. Ramos", 14, color = White, family = Inter)
                }
            }
        }
        VSpace(14.dp)
        EvaluationCard(R.drawable.lucide_ic_eye, "Graduación y Refracción", { StaffPill("Distancia lejos") }) {
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                listOf("OJO", "ESFERA", "CILINDRO", "EJE", "AV").forEach {
                    Txt(it, 8, color = Slate400, family = Inter, modifier = Modifier.weight(1f), align = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
            listOf(listOf("OD", "-2.25", "-0.75", "85°"), listOf("OI", "-2.00", "-0.50", "95°")).forEach { row ->
                Row(
                    Modifier.fillMaxWidth().height(36.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFF4F6FD)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    row.forEachIndexed { index, value ->
                        Txt(
                            value,
                            10,
                            color = if (index == 0) StaffAccent else StaffInk,
                            family = Inter,
                            modifier = Modifier.weight(1f),
                            align = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { StaffPill("20/20", size = 8) }
                }
                VSpace(8.dp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MeasureBox("Distancia pupilar (DP)", "62 mm", "Ambos ojos", Modifier.weight(1f))
                MeasureBox("Adición (cerca)", "N/A", "No aplica", Modifier.weight(1f))
            }
        }
        VSpace(14.dp)
        EvaluationCard(R.drawable.lucide_ic_file_text, "Fórmula de Cristales", { Txt("Laboratorio Norte", 9, color = Slate400, family = Inter) }) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, Slate200, RoundedCornerShape(12.dp)).padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Txt("Monofocal Orgánico Alto Índice 1.60", 11, color = StaffInk, family = Inter, modifier = Modifier.weight(1f))
                    StaffPill("Recomendado")
                }
                VSpace(10.dp)
                Txt("Tratamientos ópticos formulados para fatiga digital y trabajo frente a pantallas:", 9, color = StaffIndigoText, family = Inter)
                VSpace(10.dp)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Filtro Blue Block", "Antirreflejo Premium", "Hidrofóbico").forEach {
                        Box(
                            Modifier.clip(RoundedCornerShape(6.dp)).border(1.dp, AccentLight, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 5.dp),
                        ) { Txt(it, 8, color = StaffAccent, family = Inter) }
                    }
                }
            }
        }
        VSpace(14.dp)
        EvaluationCard(R.drawable.lucide_ic_eye, "Armazón Seleccionado", { StaffPill("En stock (2)") }) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, Slate200, RoundedCornerShape(12.dp)).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_eval_frame),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(width = 72.dp, height = 56.dp).clip(RoundedCornerShape(8.dp)),
                )
                HSpace(10.dp)
                Column {
                    Txt("RAY-BAN", 7, color = StaffAccent, family = Inter)
                    Txt("Clubmaster Classic Carey", 12, color = StaffInk, family = Inter)
                    Txt("Ref: #RB5154 · Calibre 49-21", 7, color = Slate400, family = Inter)
                    VSpace(4.dp)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Txt("$145.000", 13, weight = FontWeight.SemiBold, color = StaffInk, family = Inter)
                        Txt(" CLP", 7, color = Slate400, family = Inter)
                    }
                }
            }
        }
        VSpace(14.dp)
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(StaffBlue50).border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(16.dp)).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LIcon(R.drawable.lucide_ic_settings, StaffAccent, 20.dp)
            HSpace(14.dp)
            Column(Modifier.weight(1f)) {
                Txt("Estado de la atención", 10, color = StaffInk, family = Inter)
                Txt("Listo para generar orden al taller", 8, color = StaffInk, family = Inter)
            }
            LIcon(R.drawable.lucide_ic_check, StaffInk, 18.dp)
        }
        VSpace(14.dp)
        StaffButton("Confirmar y enviar a laboratorio", { nav.navigate(Routes.S_WORK_ORDER) }, R.drawable.lucide_ic_chevron_right)
        VSpace(10.dp)
        StaffButton(
            "Enviar receta por WhatsApp / Email",
            { Toast.makeText(context, "Receta enviada al paciente", Toast.LENGTH_SHORT).show() },
            R.drawable.lucide_ic_mail,
            background = White,
            contentColor = StaffInk,
            border = StaffBorder,
        )
        VSpace(24.dp)
    }
}

@Composable
private fun EvaluationCard(
    icon: Int,
    title: String,
    trailing: @Composable () -> Unit,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(Modifier.fillMaxWidth().staffCard(radius = 20.dp).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StaffIconTile(icon, size = 32.dp, background = Color(0xFFEEF2FF), radius = 10.dp)
            HSpace(12.dp)
            Txt(title, 13, color = StaffInk, family = Inter, modifier = Modifier.weight(1f))
            trailing()
        }
        VSpace(12.dp)
        content()
    }
}

@Composable
private fun MeasureBox(label: String, value: String, detail: String, modifier: Modifier) {
    Row(
        modifier.clip(RoundedCornerShape(10.dp)).border(1.dp, Slate200, RoundedCornerShape(10.dp)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Txt(label, 7, color = StaffInk, family = Inter)
            VSpace(6.dp)
            Txt(detail, 7, color = StaffInk, family = Inter)
        }
        Txt(value, 13, color = StaffAccent, family = Inter)
    }
}

@Composable
fun WorkOrderScreen(nav: NavController) {
    val vm = staffViewModel()
    val context = LocalContext.current
    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Orden de trabajo", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(16.dp)
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFDCE9FF), Color(0xFFF1F5FF))))
                .padding(20.dp),
        ) {
            StaffPill("Lista para crear", background = White)
            VSpace(14.dp)
            Txt("OT-2048 · Valentina Silva", 20, color = StaffInk, family = Inter)
            VSpace(6.dp)
            Txt("Venta #V-1038 · Entrega 28 de junio", 10, color = StaffAccent, family = Inter)
        }
        VSpace(24.dp)
        Txt("Especificaciones", 17, weight = FontWeight.Bold, color = StaffInk, family = Inter)
        VSpace(12.dp)
        Column(Modifier.fillMaxWidth().staffCard().padding(16.dp)) {
            SpecLine(R.drawable.lucide_ic_eye, "Montura", "Nova N-24 · Azul · 52–18–140")
            VSpace(12.dp)
            Divider(StaffBorder)
            VSpace(12.dp)
            SpecLine(R.drawable.lucide_ic_file_text, "Lentes", "Monofocal 1.60 · Antirreflejo")
        }
        VSpace(14.dp)
        Column(Modifier.fillMaxWidth().staffCard()) {
            listOf(listOf("Ojo", "Esfera", "Cilindro", "Eje"), listOf("OD", "-1.25", "-0.50", "175°"), listOf("OI", "-1.00", "-0.75", "10°"))
                .forEachIndexed { rowIndex, row ->
                    if (rowIndex > 0) Divider(StaffBorder)
                    Row(Modifier.fillMaxWidth().height(if (rowIndex == 0) 42.dp else 44.dp), verticalAlignment = Alignment.CenterVertically) {
                        row.forEachIndexed { col, value ->
                            Txt(
                                value,
                                if (rowIndex == 0) 9 else 11,
                                color = if (col == 0 && rowIndex > 0) StaffAccent else StaffInk,
                                family = Inter,
                                modifier = Modifier.weight(1f),
                                align = androidx.compose.ui.text.style.TextAlign.Center,
                            )
                        }
                    }
                }
        }
        VSpace(36.dp)
        StaffButton("Crear y enviar a producción", {
            vm.markWorkOrderSent()
            Toast.makeText(context, "Orden OT-2048 enviada a producción", Toast.LENGTH_SHORT).show()
            nav.navigate(Routes.S_PRODUCTION) { popUpTo(Routes.S_HOME) }
        })
        VSpace(24.dp)
    }
}

@Composable
private fun SpecLine(icon: Int, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        LIcon(icon, StaffInk, 22.dp)
        HSpace(14.dp)
        Column {
            Txt(label, 9, color = StaffIndigoText, family = Inter)
            VSpace(4.dp)
            Txt(value, 12, color = StaffInk, family = Inter)
        }
    }
}

@Composable
fun ProductionScreen(nav: NavController) {
    val vm = staffViewModel()
    val state by vm.uiState.collectAsState()
    var stage by rememberSaveable { mutableIntStateOf(ProductionStage.IN_WORKSHOP.ordinal) }
    val orders = state.productionOrders.filter { it.stage.ordinal == stage }
    val stageColors = listOf(Color(0xFFAAB6DD), Color(0xFF1E3D79), White, White, White)

    StaffScreen(
        nav = nav,
        tab = null,
        showBottomBar = false,
        topBar = { StaffBackTopBar("Producción", onBack = { nav.popBackStack() }) },
    ) {
        VSpace(20.dp)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProductionStage.entries.forEachIndexed { index, item ->
                val selected = index == stage
                val count = state.productionOrders.count { it.stage == item }
                Column(
                    Modifier
                        .width(94.dp)
                        .height(58.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Color(0xFF1E3D79) else if (index == 0) stageColors[0] else White)
                        .border(1.dp, if (selected) Color(0xFF1E3D79) else Lavender, RoundedCornerShape(12.dp))
                        .tap { stage = index },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Txt("$count", 13, color = if (selected) White else StaffHeader, family = Inter)
                    VSpace(4.dp)
                    Txt(item.label, 8, color = if (selected) White else StaffInk, family = Inter)
                }
            }
        }
        VSpace(26.dp)
        Txt(
            "${ProductionStage.entries[stage].label} · ${orders.size}",
            18,
            weight = FontWeight.Bold,
            color = StaffInk,
            family = Inter,
            modifier = Modifier.padding(start = 4.dp),
        )
        VSpace(14.dp)
        if (orders.isEmpty()) Txt("No hay órdenes en esta etapa.", 12, color = Slate400, family = Inter)
        orders.forEach { order ->
            Column(Modifier.fillMaxWidth().staffCard().padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (order.needsAttention) {
                        StaffPill("Requiere atención", icon = R.drawable.lucide_ic_bell)
                    } else {
                        StaffPill(ProductionStage.entries[stage].label, icon = R.drawable.lucide_ic_clock)
                    }
                    androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                    Txt(order.code, 9, color = StaffInk, family = Inter)
                }
                VSpace(12.dp)
                Txt(order.patient, 14, color = StaffInk, family = Inter)
                VSpace(4.dp)
                Txt(order.detail, 9, color = StaffIndigoText, family = Inter)
                VSpace(12.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LIcon(R.drawable.lucide_ic_calendar, StaffAccent, 14.dp)
                    HSpace(8.dp)
                    Txt(order.due, 10, color = StaffAccent, family = Inter)
                }
            }
            VSpace(10.dp)
        }
        if (state.productionFromApi) {
            VSpace(6.dp)
            Txt("Órdenes sincronizadas con el backend.", 10, color = Slate400, family = Inter)
        }
        VSpace(24.dp)
    }
}
