package com.logix.optiflow.presentation.notifications.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.domain.model.notification.NotificationType
import com.logix.optiflow.presentation.navigation.Routes
import com.logix.optiflow.presentation.notifications.NotificationRowUi
import com.logix.optiflow.presentation.notifications.NotificationsViewModel
import com.logix.optiflow.presentation.patient.components.PatientScreen
import com.logix.optiflow.presentation.shared.components.Divider
import com.logix.optiflow.presentation.shared.components.ErrorBanner
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.IconTile
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.PatientDarkTopBar
import com.logix.optiflow.presentation.shared.components.SolidButton
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.theme.DeepNavy
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.NavyBlue
import com.logix.optiflow.presentation.theme.OpenSansCondensed
import com.logix.optiflow.presentation.theme.Periwinkle
import com.logix.optiflow.presentation.theme.SkyTint
import com.logix.optiflow.presentation.theme.Slate300
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.Slate500
import com.logix.optiflow.presentation.theme.White

@Composable
fun NotificationsScreen(nav: NavController) {
    val viewModel: NotificationsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    var survey by remember { mutableStateOf<NotificationRowUi?>(null) }
    // Al volver a la app (por ejemplo, tocando un push) se recarga la lista.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose {}
    }

    PatientScreen(
        nav = nav,
        tab = null,
        topBar = { PatientDarkTopBar("Notificaciones", onBack = { nav.popBackStack() }) },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            VSpace(16.dp)
            Txt(
                "Marcar leídas",
                12,
                weight = FontWeight.Bold,
                color = Electric,
                modifier = Modifier.align(Alignment.End).tap(viewModel::markAllRead),
            )
            if (state.isLoading && state.sections.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Electric)
                }
            }
            ErrorBanner(state.errorMessage, Modifier.padding(top = 12.dp))
            state.sections.forEach { section ->
                VSpace(14.dp)
                Txt(section.title, 15, weight = FontWeight.SemiBold, color = DeepNavy, modifier = Modifier.padding(start = 10.dp))
                VSpace(8.dp)
                section.items.forEach { row ->
                    NotificationRow(
                        row = row,
                        onClick = {
                            val item = row.item
                            if (item.type == NotificationType.SURVEY) {
                                // Responder la encuesta también la marca como leída en el backend.
                                if (item.surveyRating == null) survey = row
                            } else {
                                viewModel.onNotificationClick(row)
                                when (item.type) {
                                    NotificationType.APPOINTMENT ->
                                        nav.navigate(item.appointmentId?.let { Routes.appointment(it.toString()) } ?: Routes.P_APPOINTMENTS)
                                    NotificationType.ORDER ->
                                        nav.navigate(item.workOrderId?.let { Routes.order(it.toString()) } ?: Routes.P_ORDERS)
                                    NotificationType.HEALTH -> nav.navigate(Routes.book())
                                    NotificationType.PROMO -> nav.navigate(Routes.P_PROFILE)
                                    NotificationType.SURVEY -> Unit
                                }
                            }
                        },
                    )
                }
            }
            if (!state.isLoading && state.errorMessage == null && state.sections.isEmpty()) {
                Txt(
                    "No tienes notificaciones por ahora.",
                    13,
                    color = Slate500,
                    align = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                )
            }
            VSpace(20.dp)
        }
    }

    survey?.let { row ->
        SurveyDialog(
            title = row.item.message,
            onDismiss = { survey = null },
            onSend = { rating, comment, onDone ->
                viewModel.answerSurvey(row.item.id, rating, comment) { error ->
                    onDone(error)
                    if (error == null) survey = null
                }
            },
        )
    }
}

@Composable
private fun SurveyDialog(
    title: String,
    onDismiss: () -> Unit,
    onSend: (rating: Int, comment: String, onDone: (String?) -> Unit) -> Unit,
) {
    val context = LocalContext.current
    var rating by rememberSaveable { mutableIntStateOf(0) }
    var comment by rememberSaveable { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(White)
                .padding(20.dp),
        ) {
            Txt("Encuesta de satisfacción", 17, weight = FontWeight.Bold, color = DeepNavy)
            VSpace(6.dp)
            Txt(title, 13, color = Slate500)
            VSpace(14.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                (1..5).forEach { value ->
                    LIcon(
                        R.drawable.lucide_ic_star,
                        if (value <= rating) Electric else Slate300,
                        30.dp,
                        Modifier.tap { rating = value },
                    )
                }
            }
            VSpace(14.dp)
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, SkyTint, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                if (comment.isEmpty()) Txt("Cuéntanos más (opcional)", 13, color = Slate400)
                BasicTextField(
                    value = comment,
                    onValueChange = { if (it.length <= 500) comment = it },
                    textStyle = TextStyle(fontSize = 13.sp, color = DeepNavy),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            VSpace(16.dp)
            SolidButton(
                text = if (sending) "Enviando..." else "Enviar respuesta",
                onClick = {
                    if (rating == 0) {
                        Toast.makeText(context, "Elige de 1 a 5 estrellas", Toast.LENGTH_SHORT).show()
                    } else if (!sending) {
                        sending = true
                        onSend(rating, comment) { error ->
                            sending = false
                            if (error != null) Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                height = 46.dp,
                fontSize = 15,
            )
        }
    }
}

@Composable
private fun NotificationRow(row: NotificationRowUi, onClick: () -> Unit) {
    val icon =
        when (row.item.type) {
            NotificationType.APPOINTMENT -> R.drawable.lucide_ic_calendar
            NotificationType.ORDER -> R.drawable.lucide_ic_package
            NotificationType.HEALTH -> R.drawable.lucide_ic_eye
            NotificationType.PROMO -> R.drawable.lucide_ic_megaphone
            NotificationType.SURVEY -> R.drawable.lucide_ic_star
        }
    Column(Modifier.fillMaxWidth().tap(onClick)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp)) {
            IconTile(icon, Periwinkle, NavyBlue, size = 40.dp, iconSize = 20.dp, radius = 12.dp)
            HSpace(14.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Txt(
                    row.item.title,
                    13,
                    weight = if (row.isRead) FontWeight.Medium else FontWeight.Bold,
                    color = DeepNavy,
                )
                Txt(row.item.message, 12, color = Slate500, family = OpenSansCondensed)
                if (row.item.type == NotificationType.SURVEY) {
                    val rating = row.item.surveyRating
                    Txt(
                        if (rating != null) "Respondiste con $rating de 5 estrellas" else "Toca para responder",
                        11,
                        weight = FontWeight.SemiBold,
                        color = Electric,
                    )
                }
                VSpace(6.dp)
                Txt(row.timeLabel, 10, color = Slate400)
            }
            when {
                !row.isRead ->
                    Box(Modifier.padding(top = 4.dp).size(8.dp).clip(CircleShape).background(Electric))
                row.item.type != NotificationType.SURVEY ->
                    LIcon(R.drawable.lucide_ic_chevron_right, NavyBlue, 20.dp, Modifier.padding(top = 18.dp))
            }
        }
        Divider(SkyTint)
    }
}

