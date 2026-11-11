package com.logix.optiflow.presentation.shared.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.logix.optiflow.presentation.theme.DeepNavy
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.Jakarta
import com.logix.optiflow.presentation.theme.White

/** Texto con los parámetros tipográficos del diseño (px de Figma = sp). */
@Composable
fun Txt(
    text: String,
    size: Int,
    modifier: Modifier = Modifier,
    weight: FontWeight = FontWeight.Normal,
    color: Color = DeepNavy,
    family: FontFamily = Jakarta,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    align: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    decoration: TextDecoration? = null,
) {
    Text(
        text = text,
        modifier = modifier,
        style =
            TextStyle(
                fontFamily = family,
                fontWeight = weight,
                fontSize = size.sp,
                color = color,
                lineHeight = lineHeight,
                letterSpacing = letterSpacing,
                textAlign = align ?: TextAlign.Unspecified,
                textDecoration = decoration,
            ),
        maxLines = maxLines,
        overflow = if (maxLines == Int.MAX_VALUE) TextOverflow.Clip else TextOverflow.Ellipsis,
    )
}

@Composable
fun LIcon(
    @DrawableRes res: Int,
    tint: Color,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(res),
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(size),
    )
}

/** Click sin ripple para elementos que en el diseño no tienen estado presionado. */
@Composable
fun Modifier.tap(onClick: () -> Unit): Modifier =
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick,
    )

fun Modifier.card(
    radius: Dp = 16.dp,
    background: Color = White,
    border: Color? = null,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 0.dp,
    shadowColor: Color = Color(0x141B3162),
): Modifier {
    val shape = RoundedCornerShape(radius)
    var result = this
    if (elevation > 0.dp) {
        result =
            result.shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor,
            )
    }
    result = result.clip(shape).background(background, shape)
    if (border != null) result = result.border(BorderStroke(borderWidth, border), shape)
    return result
}

@Composable
fun IconTile(
    @DrawableRes icon: Int,
    background: Color,
    tint: Color,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    radius: Dp = 12.dp,
    shape: Shape = RoundedCornerShape(radius),
) {
    Box(
        modifier = Modifier.size(size).clip(shape).background(background),
        contentAlignment = Alignment.Center,
    ) {
        LIcon(icon, tint, iconSize)
    }
}

@Composable
fun Pill(
    text: String,
    background: Color,
    color: Color,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    size: Int = 11,
    family: FontFamily = Jakarta,
    weight: FontWeight = FontWeight.Bold,
    padding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 3.dp),
    border: Color? = null,
    iconSize: Dp = 12.dp,
) {
    Row(
        modifier =
            modifier
                .clip(CircleShape)
                .background(background)
                .then(if (border != null) Modifier.border(1.dp, border, CircleShape) else Modifier)
                .padding(padding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (icon != null) LIcon(icon, color, iconSize)
        Txt(text, size, weight = weight, color = color, family = family, maxLines = 1)
    }
}

@Composable
fun SolidButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = Electric,
    contentColor: Color = White,
    height: Dp = 50.dp,
    radius: Dp = 14.dp,
    family: FontFamily = Jakarta,
    fontSize: Int = 16,
    weight: FontWeight = FontWeight.Bold,
    @DrawableRes leadingIcon: Int? = null,
    @DrawableRes trailingIcon: Int? = null,
    border: Color? = null,
    elevation: Dp = 0.dp,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(height)
                .card(
                    radius = radius,
                    background = if (enabled) background else background.copy(alpha = 0.5f),
                    border = border,
                    elevation = elevation,
                    shadowColor = background.copy(alpha = 0.35f),
                ).clickable(enabled = enabled && !loading, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = contentColor,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp),
            )
        } else {
            if (leadingIcon != null) {
                LIcon(leadingIcon, contentColor, 18.dp)
                Spacer(Modifier.width(10.dp))
            }
            Txt(text, fontSize, weight = weight, color = contentColor, family = family)
            if (trailingIcon != null) {
                Spacer(Modifier.width(10.dp))
                LIcon(trailingIcon, contentColor, 16.dp)
            }
        }
    }
}

@Composable
fun Divider(color: Color, modifier: Modifier = Modifier, thickness: Dp = 1.dp) {
    Box(modifier.fillMaxWidth().height(thickness).background(color))
}

@Composable
fun VSpace(height: Dp) = Spacer(Modifier.height(height))

@Composable
fun HSpace(width: Dp) = Spacer(Modifier.width(width))

@Composable
fun ErrorBanner(message: String?, modifier: Modifier = Modifier) {
    if (message.isNullOrBlank()) return
    Box(
        modifier
            .fillMaxWidth()
            .card(radius = 12.dp, background = com.logix.optiflow.presentation.theme.ErrorBg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Txt(message, 12, weight = FontWeight.SemiBold, color = com.logix.optiflow.presentation.theme.ErrorRed)
    }
}
