package com.logix.optiflow.presentation.notifications.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.theme.DeepNavy
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.NavyBlue
import com.logix.optiflow.presentation.theme.OpenSansCondensed
import com.logix.optiflow.presentation.theme.Periwinkle
import com.logix.optiflow.presentation.theme.SkyTint
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.Slate500

@Composable
fun NotificationsScreen(nav: NavController) {
    val viewModel: NotificationsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

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
                            viewModel.onNotificationClick(row.item.id)
                            when (row.item.type) {
                                NotificationType.APPOINTMENT -> nav.navigate(Routes.P_APPOINTMENTS)
                                NotificationType.ORDER -> nav.navigate(Routes.P_ORDERS)
                                NotificationType.HEALTH -> nav.navigate(Routes.P_HISTORY)
                                NotificationType.PROMO -> nav.navigate(Routes.P_CATALOG)
                            }
                        },
                    )
                }
            }
            VSpace(20.dp)
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
                VSpace(6.dp)
                Txt(row.timeLabel, 10, color = Slate400)
            }
            when {
                !row.isRead ->
                    Box(Modifier.padding(top = 4.dp).size(8.dp).clip(CircleShape).background(Electric))
                row.item.navigable ->
                    LIcon(R.drawable.lucide_ic_chevron_right, NavyBlue, 20.dp, Modifier.padding(top = 18.dp))
            }
        }
        Divider(SkyTint)
    }
}

