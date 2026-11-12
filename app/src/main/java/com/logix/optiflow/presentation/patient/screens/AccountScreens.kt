package com.logix.optiflow.presentation.patient.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.logix.optiflow.presentation.patient.EditProfileViewModel
import com.logix.optiflow.presentation.patient.components.PatientScreen
import com.logix.optiflow.presentation.patient.components.patientViewModel
import com.logix.optiflow.presentation.shared.components.ErrorBanner
import com.logix.optiflow.presentation.shared.components.PatientLightTopBar
import com.logix.optiflow.presentation.shared.components.PatientTab
import com.logix.optiflow.presentation.shared.components.ScreenFrame
import com.logix.optiflow.presentation.shared.components.SolidButton
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.card
import com.logix.optiflow.presentation.theme.DeepNavy
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.Jakarta
import com.logix.optiflow.presentation.theme.Lavender
import com.logix.optiflow.presentation.theme.SkyTint
import com.logix.optiflow.presentation.theme.Slate300
import com.logix.optiflow.presentation.theme.Slate500
import com.logix.optiflow.presentation.theme.Slate700
import com.logix.optiflow.presentation.theme.White

@Composable
fun EditProfileScreen(nav: NavController, vm: EditProfileViewModel = hiltViewModel()) {
    val patientVm = patientViewModel()
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(state.saved) {
        if (state.saved) {
            Toast.makeText(context, "Perfil actualizado.", Toast.LENGTH_SHORT).show()
            // Inicio y Mi Perfil leen el nombre de la sesión, que ya se actualizó.
            patientVm.refresh()
            nav.popBackStack()
        }
    }

    PatientScreen(
        nav = nav,
        tab = PatientTab.PROFILE,
        topBar = { PatientLightTopBar("Editar perfil", onBack = { nav.popBackStack() }) },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            VSpace(8.dp)
            if (state.isLoading) {
                Txt("Cargando tus datos...", 14, color = Slate500)
                return@Column
            }
            Column(
                Modifier.fillMaxWidth().card(radius = 16.dp, border = Lavender, elevation = 2.dp).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ProfileField("Nombre completo", state.name, vm::onNameChange, "Tu nombre y apellido")
                ProfileField("Teléfono", state.phone, vm::onPhoneChange, "999 888 777", KeyboardType.Phone)
                ProfileField(
                    "Fecha de nacimiento (opcional)",
                    state.birthDate,
                    vm::onBirthDateChange,
                    "24/05/1990",
                    KeyboardType.Number,
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FieldLabel("Correo")
                    Txt(state.email, 13, color = Slate500)
                    Txt("El correo es tu usuario para ingresar y no se puede cambiar.", 11, color = Slate500)
                }
            }
            VSpace(14.dp)
            ErrorBanner(state.errorMessage)
            VSpace(10.dp)
            SolidButton(
                if (state.isSaving) "Guardando..." else "Guardar cambios",
                onClick = vm::save,
                height = 48.dp,
                radius = 12.dp,
            )
            VSpace(16.dp)
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Txt(text, 12, weight = FontWeight.Bold, color = DeepNavy)
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(label)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            cursorBrush = SolidColor(Electric),
            textStyle = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = DeepNavy),
            decorationBox = { inner ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(shape)
                        .background(White)
                        .border(1.dp, SkyTint, shape)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty()) Txt(placeholder, 14, weight = FontWeight.Medium, color = Slate500)
                        inner()
                    }
                }
            },
        )
    }
}

/** Pantallas de texto de Configuración. */
enum class InfoTopic(val title: String) {
    PRIVACY("Privacidad y seguridad"),
    HELP("Ayuda y documentación"),
    TERMS("Términos y privacidad"),
    ;

    companion object {
        fun from(value: String?): InfoTopic = entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: HELP
    }
}

private class InfoSection(val title: String, val lines: List<String>)

@Composable
fun InfoScreen(nav: NavController, topic: InfoTopic, staff: Boolean) {
    val sections = sectionsFor(topic, staff)
    val content: @Composable ColumnScope.() -> Unit = {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            VSpace(4.dp)
            sections.forEach { section ->
                Column(Modifier.fillMaxWidth().card(radius = 16.dp, border = Lavender, elevation = 2.dp).padding(16.dp)) {
                    Txt(section.title, 15, weight = FontWeight.Bold, color = DeepNavy)
                    section.lines.forEach { line ->
                        VSpace(8.dp)
                        Txt(line, 13, color = Slate700, lineHeight = 19.sp)
                    }
                }
                VSpace(12.dp)
            }
            VSpace(12.dp)
            Txt(
                "OptiFlow · Versión 1.0.0",
                12,
                color = Slate300,
                align = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            VSpace(24.dp)
        }
    }
    val topBar: @Composable () -> Unit = { PatientLightTopBar(topic.title, onBack = { nav.popBackStack() }) }
    if (staff) {
        ScreenFrame(topBar = topBar, content = content)
    } else {
        PatientScreen(nav = nav, tab = PatientTab.PROFILE, topBar = topBar, content = content)
    }
}

private fun sectionsFor(topic: InfoTopic, staff: Boolean): List<InfoSection> =
    when (topic) {
        InfoTopic.PRIVACY -> PRIVACY
        InfoTopic.TERMS -> TERMS
        InfoTopic.HELP -> if (staff) STAFF_HELP else PATIENT_HELP
    }

private val PRIVACY =
    listOf(
        InfoSection(
            "Tu cuenta",
            listOf(
                "Tu contraseña se guarda cifrada. Nadie del equipo de OptiFlow ni de la óptica puede verla.",
                "Tu sesión vence a las 24 horas. Después tendrás que volver a ingresar.",
                "Si usas un celular compartido, cierra sesión desde Mi Perfil.",
            ),
        ),
        InfoSection(
            "Tus datos de salud",
            listOf(
                "Tu historial clínico y tus recetas solo los ven tú y el personal autorizado de la óptica.",
                "Ningún otro paciente puede consultar tu información.",
            ),
        ),
        InfoSection(
            "Permisos del celular",
            listOf(
                "Ubicación: para mostrarte las ópticas cercanas en el mapa. Puedes negarla y buscar las ópticas igual.",
                "Cámara: para el probador virtual de monturas. La imagen se procesa en tu celular y no se envía a ningún servidor.",
                "Notificaciones: para avisarte de tus citas y del avance de tu pedido.",
            ),
        ),
        InfoSection(
            "Pagos",
            listOf("Los pagos con tarjeta o Yape se hacen en la página de Mercado Pago. OptiFlow no ve ni guarda los datos de tu tarjeta."),
        ),
        InfoSection(
            "Sin conexión",
            listOf("Lo último que consultaste queda guardado en este celular para que lo veas sin internet. Se borra al cerrar sesión."),
        ),
    )

private val PATIENT_HELP =
    listOf(
        InfoSection(
            "¿Cómo reservo una cita?",
            listOf("En Citas, toca Reservar cita. Elige la óptica, el día y el horario, y confirma. También puedes empezar desde una óptica en Buscar."),
        ),
        InfoSection(
            "¿Cómo cancelo o cambio una cita?",
            listOf("En Citas, abre la cita y toca Cancelar cita o Reprogramar cita."),
        ),
        InfoSection(
            "¿Dónde veo mi receta?",
            listOf("En Mi Perfil → Mis recetas. El optómetra la registra después de tu evaluación en la óptica."),
        ),
        InfoSection(
            "¿Cómo sé cuándo están listos mis lentes?",
            listOf("En Pedidos ves en qué etapa está tu pedido: taller, control de calidad y listo para recoger. Además te llega una notificación."),
        ),
        InfoSection(
            "¿Cómo pago?",
            listOf("En Mi Perfil → Mis compras, toca Pagar con tarjeta o Yape. También puedes pagar en efectivo en la óptica."),
        ),
        InfoSection(
            "¿Qué son los puntos OptiFlow?",
            listOf("Recibes 100 puntos en tu cumpleaños. Tu saldo aparece en Mi Perfil."),
        ),
        InfoSection(
            "¿Cómo cambio mis datos?",
            listOf("En Mi Perfil, toca Editar perfil para cambiar tu nombre, teléfono o fecha de nacimiento."),
        ),
        InfoSection("¿Necesitas más ayuda?", listOf("Acércate a tu óptica y el personal te ayudará.")),
    )

private val STAFF_HELP =
    listOf(
        InfoSection(
            "Agenda",
            listOf(
                "En Citas ves las citas de tu óptica por día. Toca un día de la semana para cambiar de fecha.",
                "Con Nueva cita eliges al paciente y un turno libre. La agenda pasa al día de la cita agendada.",
            ),
        ),
        InfoSection(
            "Pacientes",
            listOf("En Pacientes buscas a un paciente y abres su ficha. Con Nuevo paciente lo registras en la óptica."),
        ),
        InfoSection(
            "Cotizaciones y ventas",
            listOf("En Más → Cotizaciones preparas la cotización del paciente. En Ventas y pagos registras la venta y su pago."),
        ),
        InfoSection(
            "Inventario",
            listOf("En Inventario ves el catálogo y el stock de tu óptica. Con Escanear buscas una montura por su código."),
        ),
        InfoSection(
            "Horarios de atención",
            listOf("El administrador abre los turnos en Más → Horarios de atención. Sin turnos abiertos no se pueden reservar citas."),
        ),
        InfoSection(
            "Tu acceso",
            listOf("Lo que ves en el menú depende de tu rol. Si te falta una opción, pídesela al administrador de tu óptica."),
        ),
    )

private val TERMS =
    listOf(
        InfoSection(
            "Uso de la app",
            listOf(
                "OptiFlow conecta a pacientes con ópticas para reservar citas, ver recetas, seguir pedidos y pagar.",
                "La atención visual y las recetas son responsabilidad del optómetra de cada óptica.",
                "El probador virtual es una referencia: la montura puede verse un poco distinta en persona.",
            ),
        ),
        InfoSection(
            "Tu información",
            listOf(
                "Usamos tu nombre, correo, teléfono, citas, recetas y compras solo para darte el servicio.",
                "No vendemos ni compartimos tus datos con terceros. La excepción es Mercado Pago, solo para procesar tus pagos.",
                "Puedes cambiar tu nombre y teléfono en Mi Perfil → Editar perfil. Para eliminar tus datos, pídelo en tu óptica.",
            ),
        ),
        InfoSection(
            "Tu cuenta",
            listOf(
                "Eres responsable de no compartir tu contraseña.",
                "Si no vas a asistir a una cita, cancélala para que el horario quede libre para otro paciente.",
            ),
        ),
    )
