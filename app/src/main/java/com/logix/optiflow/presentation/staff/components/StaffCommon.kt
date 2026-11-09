package com.logix.optiflow.presentation.staff.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.presentation.navigation.goStaffTab
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.Pill
import com.logix.optiflow.presentation.shared.components.ScreenFrame
import com.logix.optiflow.presentation.shared.components.SolidButton
import com.logix.optiflow.presentation.shared.components.StaffBottomBar
import com.logix.optiflow.presentation.shared.components.StaffTab
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.card
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.staff.StaffViewModel
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.Inter
import com.logix.optiflow.presentation.theme.Lavender
import com.logix.optiflow.presentation.theme.ScreenBg
import com.logix.optiflow.presentation.theme.Slate200
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.StaffAccent
import com.logix.optiflow.presentation.theme.StaffBlue50
import com.logix.optiflow.presentation.theme.StaffBlue700
import com.logix.optiflow.presentation.theme.StaffBorder
import com.logix.optiflow.presentation.theme.StaffField
import com.logix.optiflow.presentation.theme.StaffHeader
import com.logix.optiflow.presentation.theme.StaffInk
import com.logix.optiflow.presentation.theme.White

val StaffSoft = Color(0xFFF1F5FF)
val StaffIndigoText = Color(0xFF1E3A8A)

@Composable
fun staffViewModel(): StaffViewModel {
    val activity = LocalContext.current as ComponentActivity
    return hiltViewModel(viewModelStoreOwner = activity)
}

/** Pantalla del personal: cabecera, contenido desplazable y barra inferior opcional. */
@Composable
fun StaffScreen(
    nav: NavController,
    tab: StaffTab?,
    topBar: @Composable () -> Unit,
    showBottomBar: Boolean = true,
    scrollable: Boolean = true,
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    ScreenFrame(
        background = ScreenBg,
        topBar = topBar,
        bottomBar = { if (showBottomBar) StaffBottomBar(tab) { nav.goStaffTab(it) } },
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                    .then(if (showBottomBar) Modifier else Modifier.navigationBarsPadding())
                    .padding(horizontal = 16.dp),
                content = content,
            )
            overlay()
        }
    }
}

fun Modifier.staffCard(radius: Dp = 18.dp, background: Color = White, border: Color = StaffBorder): Modifier =
    card(radius = radius, background = background, border = border)

@Composable
fun StaffHero(
    modifier: Modifier = Modifier,
    background: Color = StaffHeader,
    radius: Dp = 26.dp,
    padding: PaddingValues = PaddingValues(horizontal = 22.dp, vertical = 22.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(radius)).background(background).padding(padding),
        content = content,
    )
}

@Composable
fun StaffPill(
    text: String,
    icon: Int? = R.drawable.lucide_ic_check,
    background: Color = StaffBlue50,
    color: Color = StaffBlue700,
    size: Int = 9,
    modifier: Modifier = Modifier,
) {
    Pill(
        text = text,
        background = background,
        color = color,
        icon = icon,
        size = size,
        family = Inter,
        weight = FontWeight.SemiBold,
        padding = PaddingValues(horizontal = 9.dp, vertical = 5.dp),
        iconSize = 13.dp,
        modifier = modifier,
    )
}

@Composable
fun StaffSectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Txt(
            title,
            16,
            weight = FontWeight.Bold,
            color = StaffInk,
            family = Inter,
            letterSpacing = (-0.16).sp,
            modifier = Modifier.weight(1f),
        )
        if (action != null) {
            Txt(action, 11, weight = FontWeight.SemiBold, color = StaffAccent, family = Inter, modifier = Modifier.tap(onAction))
        }
    }
}

@Composable
fun StaffButton(
    text: String,
    onClick: () -> Unit,
    icon: Int? = null,
    background: Color = StaffAccent,
    contentColor: Color = White,
    border: Color? = null,
    height: Dp = 50.dp,
    modifier: Modifier = Modifier,
) {
    SolidButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        background = background,
        contentColor = contentColor,
        height = height,
        radius = 14.dp,
        family = Inter,
        fontSize = 14,
        weight = FontWeight.SemiBold,
        leadingIcon = icon,
        border = border,
    )
}

@Composable
fun StaffInitials(text: String, size: Dp = 43.dp, background: Color = Lavender, color: Color = Electric, fontSize: Int = 11) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(14.dp)).background(background),
        contentAlignment = Alignment.Center,
    ) {
        Txt(text, fontSize, color = color, family = Inter)
    }
}

@Composable
fun StaffIconTile(icon: Int, size: Dp = 35.dp, background: Color = Color(0xFFF4F5FF), tint: Color = StaffAccent, radius: Dp = 11.dp) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(radius)).background(background),
        contentAlignment = Alignment.Center,
    ) {
        LIcon(icon, tint, 20.dp)
    }
}

@Composable
fun StaffLabel(text: String, trailing: String? = null) {
    Column(Modifier.fillMaxWidth()) {
        Txt(text, 13, color = StaffInk, family = Inter)
        if (trailing != null) {
            Txt(
                trailing,
                11,
                color = StaffAccent,
                family = Inter,
                modifier = Modifier.align(Alignment.End).padding(top = 8.dp),
            )
        }
    }
}

@Composable
fun StaffInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: Int? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    height: Dp = 50.dp,
    singleLine: Boolean = true,
    trailing: @Composable () -> Unit = {},
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        cursorBrush = SolidColor(StaffAccent),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = TextStyle(fontFamily = Inter, fontSize = 14.sp, color = StaffInk),
        decorationBox = { inner ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(RoundedCornerShape(14.dp))
                    .background(StaffField)
                    .border(1.dp, Slate200, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = if (singleLine) 0.dp else 12.dp),
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
            ) {
                if (icon != null) {
                    LIcon(icon, StaffInk, 20.dp)
                    HSpace(10.dp)
                }
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Txt(placeholder, if (singleLine) 14 else 12, color = Slate400, family = Inter)
                    inner()
                }
                trailing()
            }
        },
    )
}

@Composable
fun StaffSelect(text: String, icon: Int?, onClick: () -> Unit, leading: (@Composable () -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(StaffField)
            .border(1.dp, Slate200, RoundedCornerShape(14.dp))
            .tap(onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            HSpace(10.dp)
        } else if (icon != null) {
            LIcon(icon, StaffInk, 20.dp)
            HSpace(12.dp)
        }
        Txt(text, 14, color = StaffInk, family = Inter, modifier = Modifier.weight(1f), maxLines = 1)
        LIcon(R.drawable.lucide_ic_chevron_right, StaffInk, 20.dp)
    }
}

@Composable
fun StaffFab(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .height(50.dp)
            .shadow(10.dp, CircleShape, spotColor = StaffAccent)
            .clip(CircleShape)
            .background(StaffAccent)
            .tap(onClick)
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LIcon(R.drawable.lucide_ic_plus, White, 22.dp)
        Txt(text, 13, weight = FontWeight.Medium, color = White, family = Inter)
    }
}

@Composable
fun StaffChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .height(34.dp)
            .clip(CircleShape)
            .background(if (selected) StaffAccent else White)
            .border(1.dp, if (selected) StaffAccent else StaffBorder, CircleShape)
            .tap(onClick)
            .padding(horizontal = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Txt(text, 11, weight = FontWeight.Medium, color = if (selected) White else StaffInk, family = Inter, maxLines = 1)
    }
}

@Composable
fun KeyValueRow(label: String, value: String, valueSize: Int = 11, labelSize: Int = 11) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Txt(label, labelSize, color = StaffInk, family = Inter, modifier = Modifier.weight(1f))
        Txt(value, valueSize, color = StaffInk, family = Inter)
    }
}
