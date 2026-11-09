package com.logix.optiflow.presentation.shared.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.logix.optiflow.R
import com.logix.optiflow.presentation.theme.BellChip
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.Geist
import com.logix.optiflow.presentation.theme.Inter
import com.logix.optiflow.presentation.theme.Jakarta
import com.logix.optiflow.presentation.theme.NavInactive
import com.logix.optiflow.presentation.theme.Navy75
import com.logix.optiflow.presentation.theme.PatientHeader
import com.logix.optiflow.presentation.theme.Periwinkle
import com.logix.optiflow.presentation.theme.ScreenBg
import com.logix.optiflow.presentation.theme.SkyTint
import com.logix.optiflow.presentation.theme.StaffChip
import com.logix.optiflow.presentation.theme.StaffHeader
import com.logix.optiflow.presentation.theme.StaffMuted
import com.logix.optiflow.presentation.theme.StaffSubtle
import com.logix.optiflow.presentation.theme.White

enum class PatientTab(val label: String, @param:DrawableRes val icon: Int) {
    HOME("Inicio", R.drawable.lucide_ic_house),
    SEARCH("Buscar", R.drawable.lucide_ic_glasses),
    APPOINTMENTS("Citas", R.drawable.lucide_ic_calendar),
    ORDERS("Pedidos", R.drawable.lucide_ic_package),
    PROFILE("Perfil", R.drawable.lucide_ic_user),
}

enum class StaffTab(val label: String, @param:DrawableRes val icon: Int) {
    HOME("Inicio", R.drawable.lucide_ic_house),
    AGENDA("Citas", R.drawable.lucide_ic_calendar),
    PATIENTS("Pacientes", R.drawable.lucide_ic_users),
    INVENTORY("Inventario", R.drawable.lucide_ic_layout_grid),
    MORE("Más", R.drawable.lucide_ic_ellipsis),
}

/** Ajusta el color de los iconos de la barra de estado según el fondo de la cabecera. */
@Composable
fun StatusBarIcons(lightIcons: Boolean) {
    val view = androidx.compose.ui.platform.LocalView.current
    if (view.isInEditMode) return
    androidx.compose.runtime.SideEffect {
        val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
        androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !lightIcons
    }
}

/** Estructura común: cabecera + contenido + barra inferior. */
@Composable
fun ScreenFrame(
    background: Color = ScreenBg,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize().background(background)) {
        topBar()
        Column(Modifier.weight(1f).fillMaxWidth(), content = content)
        bottomBar()
    }
}

@Composable
fun PatientBottomBar(selected: PatientTab?, onSelect: (PatientTab) -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(White)
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 24.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PatientTab.entries.forEach { tab ->
            val active = tab == selected
            Column(
                modifier =
                    Modifier
                        .size(width = 64.dp, height = 50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (active) SkyTint else Color.Transparent)
                        .tap { onSelect(tab) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            ) {
                LIcon(tab.icon, if (active) Navy75 else NavInactive, 22.dp)
                Txt(
                    tab.label,
                    11,
                    weight = FontWeight.Medium,
                    color = if (active) Navy75 else NavInactive,
                    family = Geist,
                )
            }
        }
    }
}

@Composable
fun StaffBottomBar(selected: StaffTab?, tabs: List<StaffTab> = StaffTab.entries, onSelect: (StaffTab) -> Unit) {
    Column(Modifier.fillMaxWidth().background(White)) {
        Divider(StaffChip)
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(71.dp)
                    .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                val active = tab == selected
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(57.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(if (active) StaffChip else Color.Transparent)
                            .tap { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
                ) {
                    LIcon(tab.icon, if (active) StaffHeader else StaffMuted, 22.dp)
                    Txt(
                        tab.label,
                        9,
                        weight = FontWeight.SemiBold,
                        color = if (active) StaffHeader else StaffMuted,
                        family = Inter,
                    )
                }
            }
        }
    }
}

/** Cabecera oscura del paciente con botón volver (Notificaciones, Mis citas, Receta…). */
@Composable
fun PatientDarkTopBar(
    title: String,
    onBack: (() -> Unit)?,
    trailing: @Composable RowScope.() -> Unit = {},
    below: @Composable ColumnScope.() -> Unit = {},
) {
    StatusBarIcons(lightIcons = true)
    Column(Modifier.fillMaxWidth().background(PatientHeader).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Periwinkle)
                        .tap(onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    LIcon(R.drawable.lucide_ic_chevron_left, PatientHeader, 22.dp)
                }
                HSpace(16.dp)
            }
            Txt(
                title,
                22,
                weight = FontWeight.ExtraBold,
                color = White,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            trailing()
        }
        below()
    }
}

/** Cabecera clara (Buscar, Explorar monturas, Perfil, Historial, Configuración). */
@Composable
fun PatientLightTopBar(
    title: String,
    onBack: (() -> Unit)?,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    StatusBarIcons(lightIcons = false)
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(68.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE4E9FB))
                    .tap(onBack),
                contentAlignment = Alignment.Center,
            ) {
                LIcon(R.drawable.lucide_ic_chevron_left, PatientHeader, 20.dp)
            }
            HSpace(16.dp)
        }
        Txt(
            title,
            22,
            weight = FontWeight.ExtraBold,
            color = PatientHeader,
            letterSpacing = (-0.6).sp,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        trailing()
    }
}

@Composable
fun BellButton(onClick: () -> Unit, dot: Boolean = true, size: Dp = 42.dp, circle: Boolean = true) {
    Box(
        Modifier
            .size(size)
            .clip(if (circle) CircleShape else RoundedCornerShape(16.dp))
            .background(BellChip)
            .tap(onClick),
        contentAlignment = Alignment.Center,
    ) {
        LIcon(R.drawable.lucide_ic_bell, PatientHeader, 20.dp)
        if (dot) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 7.dp, end = 7.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(White)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Electric),
            )
        }
    }
}

/** Cabecera del personal clínico (Inicio, Citas y agenda, Pacientes, Inventario, Más). */
@Composable
fun StaffTopBar(
    title: String,
    eyebrow: String? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    StatusBarIcons(lightIcons = true)
    Row(
        Modifier
            .fillMaxWidth()
            .background(StaffHeader)
            .statusBarsPadding()
            .height(90.dp)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(Modifier.weight(1f)) {
            if (eyebrow != null) {
                Txt(
                    eyebrow,
                    9,
                    weight = FontWeight.SemiBold,
                    color = StaffSubtle,
                    family = Inter,
                    letterSpacing = 1.17.sp,
                )
                VSpace(3.dp)
            }
            Txt(
                title,
                20,
                weight = FontWeight.Bold,
                color = White,
                family = Inter,
                letterSpacing = (-0.6).sp,
            )
        }
        trailing()
    }
}

@Composable
fun StaffBackTopBar(
    title: String,
    onBack: () -> Unit,
    eyebrow: String? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    StatusBarIcons(lightIcons = true)
    Row(
        Modifier
            .fillMaxWidth()
            .background(StaffHeader)
            .statusBarsPadding()
            .height(90.dp)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(42.dp).clip(CircleShape).background(StaffChip).tap(onBack),
            contentAlignment = Alignment.Center,
        ) {
            LIcon(R.drawable.lucide_ic_chevron_left, StaffHeader, 20.dp)
        }
        HSpace(8.dp)
        Column(Modifier.weight(1f)) {
            if (eyebrow != null) {
                Txt(
                    eyebrow,
                    10,
                    weight = FontWeight.SemiBold,
                    color = StaffSubtle,
                    family = Inter,
                    letterSpacing = 1.sp,
                )
            }
            Txt(
                title,
                20,
                weight = FontWeight.Bold,
                color = White,
                family = Inter,
                letterSpacing = (-0.6).sp,
                maxLines = 1,
            )
        }
        trailing()
    }
}

@Composable
fun StaffBell(onClick: () -> Unit) {
    Box(
        Modifier.size(42.dp).clip(CircleShape).background(StaffChip).tap(onClick),
        contentAlignment = Alignment.Center,
    ) {
        LIcon(R.drawable.lucide_ic_bell, StaffHeader, 22.dp)
        Box(
            Modifier
                .align(Alignment.TopStart)
                .padding(start = 27.dp, top = 7.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(White)
                .padding(1.dp)
                .clip(CircleShape)
                .background(Electric),
        )
    }
}

@Composable
fun StaffCircleAction(@DrawableRes icon: Int, onClick: () -> Unit) {
    Box(
        Modifier.size(42.dp).clip(CircleShape).background(StaffChip).tap(onClick),
        contentAlignment = Alignment.Center,
    ) {
        LIcon(icon, StaffHeader, 22.dp)
    }
}

@Composable
fun SegmentChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedBg: Color,
    selectedText: Color,
    unselectedBg: Color = White,
    unselectedText: Color,
    border: Color? = null,
    family: androidx.compose.ui.text.font.FontFamily = Jakarta,
    fontSize: Int = 14,
    height: Dp = 38.dp,
    weight: FontWeight = FontWeight.SemiBold,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .height(height)
            .clip(CircleShape)
            .background(if (selected) selectedBg else unselectedBg)
            .then(
                if (!selected && border != null) Modifier.border(1.dp, border, CircleShape) else Modifier,
            ).tap(onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Txt(
            text,
            fontSize,
            weight = weight,
            color = if (selected) selectedText else unselectedText,
            family = family,
            maxLines = 1,
        )
    }
}


