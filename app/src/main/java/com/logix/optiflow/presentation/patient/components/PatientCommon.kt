package com.logix.optiflow.presentation.patient.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.presentation.navigation.goPatientTab
import com.logix.optiflow.presentation.patient.AppointmentStatus
import com.logix.optiflow.presentation.patient.PatientViewModel
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.PatientBottomBar
import com.logix.optiflow.presentation.shared.components.PatientTab
import com.logix.optiflow.presentation.shared.components.Pill
import com.logix.optiflow.presentation.shared.components.ScreenFrame
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.theme.DeepNavy
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.Periwinkle
import com.logix.optiflow.presentation.theme.ScreenBg
import com.logix.optiflow.presentation.theme.SkyTint

/** ViewModel del paciente compartido por todas las pantallas (vive en la Activity). */
@Composable
fun patientViewModel(): PatientViewModel {
    val activity = LocalContext.current as ComponentActivity
    return hiltViewModel(viewModelStoreOwner = activity)
}

@Composable
fun PatientScreen(
    nav: NavController,
    tab: PatientTab?,
    topBar: @Composable () -> Unit,
    background: Color = ScreenBg,
    content: @Composable ColumnScope.() -> Unit,
) {
    ScreenFrame(
        background = background,
        topBar = topBar,
        bottomBar = { PatientBottomBar(tab) { nav.goPatientTab(it) } },
        content = content,
    )
}

@Composable
fun DateBadge(
    day: String,
    month: String,
    width: Dp = 56.dp,
    height: Dp = 64.dp,
    background: Color = Periwinkle,
) {
    Column(
        Modifier
            .size(width, height)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .border(1.dp, SkyTint, RoundedCornerShape(16.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Txt(day, 20, weight = FontWeight.ExtraBold, color = DeepNavy, letterSpacing = (-0.5).sp)
        Txt(month, 10, weight = FontWeight.Bold, color = Electric, letterSpacing = 0.5.sp)
    }
}

@Composable
fun StatusCapsule(status: AppointmentStatus) {
    val icon =
        when (status) {
            AppointmentStatus.CONFIRMED -> R.drawable.lucide_ic_check
            AppointmentStatus.PENDING -> R.drawable.lucide_ic_clock
            AppointmentStatus.CANCELLED -> R.drawable.lucide_ic_x
        }
    Pill(
        text = status.label,
        background = SkyTint.copy(alpha = 0.5f),
        color = if (status == AppointmentStatus.CONFIRMED) DeepNavy else Electric,
        icon = icon,
        border = if (status == AppointmentStatus.CONFIRMED) null else SkyTint,
    )
}

@Composable
fun SectionHeader(
    title: String,
    action: String? = null,
    onAction: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Txt(
            title,
            16,
            weight = FontWeight.Bold,
            color = DeepNavy,
            letterSpacing = (-0.4).sp,
            modifier = Modifier.weight(1f),
        )
        if (action != null) {
            Row(
                Modifier.clip(RoundedCornerShape(8.dp)).tap(onAction).padding(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Txt(action, 12, weight = FontWeight.SemiBold, color = Electric)
                LIcon(R.drawable.lucide_ic_chevron_right, Electric, 14.dp)
            }
        }
    }
}
