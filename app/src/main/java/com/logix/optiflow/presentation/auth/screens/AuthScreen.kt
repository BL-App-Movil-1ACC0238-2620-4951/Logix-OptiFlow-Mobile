package com.logix.optiflow.presentation.auth.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.logix.optiflow.R
import com.logix.optiflow.presentation.auth.AuthUiState
import com.logix.optiflow.presentation.auth.AuthViewModel
import com.logix.optiflow.presentation.auth.UserRole
import com.logix.optiflow.presentation.shared.components.ErrorBanner
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.SolidButton
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.theme.Brand
import com.logix.optiflow.presentation.theme.CardSurface
import com.logix.optiflow.presentation.theme.DeepNavy
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.Jakarta
import com.logix.optiflow.presentation.theme.Lavender
import com.logix.optiflow.presentation.theme.LoginGradientTop
import com.logix.optiflow.presentation.theme.MintWhite
import com.logix.optiflow.presentation.theme.NavyBlue
import com.logix.optiflow.presentation.theme.OpenSansCondensed
import com.logix.optiflow.presentation.theme.Periwinkle
import com.logix.optiflow.presentation.theme.Poppins
import com.logix.optiflow.presentation.theme.SkyTint
import com.logix.optiflow.presentation.theme.Slate200
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.Slate500
import com.logix.optiflow.presentation.theme.Slate700
import com.logix.optiflow.presentation.theme.Subtitle
import com.logix.optiflow.presentation.theme.Tagline
import com.logix.optiflow.presentation.theme.White

private val CtaNavy = Color(0xFF1C3C78)

@Composable
fun AuthScreen(onAuthenticated: (UserRole) -> Unit) {
    val viewModel: AuthViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.authenticatedAs) {
        state.authenticatedAs?.let {
            viewModel.consumeAuthenticated()
            onAuthenticated(it)
        }
    }

    com.logix.optiflow.presentation.shared.components.StatusBarIcons(lightIcons = false)
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(0f to LoginGradientTop, 0.8125f to White)),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            VSpace(27.dp)
            Image(
                painter = painterResource(R.drawable.img_logo),
                contentDescription = "OptiFlow",
                modifier = Modifier.size(width = 137.dp, height = 108.dp),
            )
            Txt("OptiFlow", 32, weight = FontWeight.Bold, color = Brand, family = Poppins)
            VSpace(2.dp)
            Txt("Tu óptica, siempre cerca", 16, color = Tagline, family = OpenSansCondensed)
            VSpace(30.dp)
            AuthCard(state, viewModel)
        }
    }
}

@Composable
private fun AuthCard(state: AuthUiState, viewModel: AuthViewModel) {
    Column(
        Modifier
            .padding(horizontal = 26.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0x991D3B77),
                spotColor = Color(0x991D3B77),
            ).clip(RoundedCornerShape(20.dp))
            .background(CardSurface)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Txt(
            "Accede a OptiFlow",
            26,
            weight = FontWeight.Bold,
            color = DeepNavy,
            family = Poppins,
            letterSpacing = (-0.65).sp,
            lineHeight = 32.5.sp,
        )
        Txt(
            "Elige cómo usarás la plataforma para preparar tu espacio.",
            12,
            color = Subtitle,
            family = OpenSansCondensed,
            align = TextAlign.Center,
        )
        VSpace(12.dp)
        ModeSwitch(state.isRegisterMode, viewModel::setRegisterMode)
        VSpace(15.dp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RoleCard(
                letter = "P",
                title = "Paciente",
                description = "Mis citas, receta y monturas",
                selected = state.role == UserRole.PATIENT,
                onClick = { viewModel.setRole(UserRole.PATIENT) },
                modifier = Modifier.weight(1f),
            )
            RoleCard(
                letter = "C",
                title = "Personal clínico",
                description = "Catálogo, agenda y pacientes",
                selected = state.role == UserRole.CLINICAL,
                onClick = { viewModel.setRole(UserRole.CLINICAL) },
                modifier = Modifier.weight(1f),
            )
        }
        VSpace(18.dp)
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (state.isRegisterMode) {
                LabeledField("Nombre completo", state.name, viewModel::onNameChange, "Nombre")
            }
            LabeledField(
                "Correo electrónico",
                state.email,
                viewModel::onEmailChange,
                "correo@gmail.com",
                keyboardType = KeyboardType.Email,
                background = if (state.isRegisterMode) Color(0xFFEEF2FD) else MintWhite,
            )
            if (state.isRegisterMode && state.role == UserRole.PATIENT) {
                LabeledField(
                    "Teléfono",
                    state.phone,
                    viewModel::onPhoneChange,
                    "999 888 777",
                    keyboardType = KeyboardType.Phone,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    FieldLabel("Contraseña", Modifier.weight(1f))
                    if (state.isRegisterMode) {
                        Txt("Mín. ${AuthViewModel.MIN_PASSWORD} caracteres", 11, color = Slate400, family = Jakarta)
                    } else {
                        Txt("¿Olvidaste tu contraseña?", 11, weight = FontWeight.SemiBold, color = Electric)
                    }
                }
                AuthInput(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = "Mínimo ${AuthViewModel.MIN_PASSWORD} caracteres",
                    keyboardType = KeyboardType.Password,
                    visual =
                        if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailing = {
                        LIcon(
                            if (state.passwordVisible) R.drawable.lucide_ic_eye_off else R.drawable.lucide_ic_eye,
                            Slate500,
                            16.dp,
                            Modifier.tap(viewModel::togglePasswordVisibility),
                        )
                    },
                )
            }
            if (state.isRegisterMode) TermsRow(state.acceptedTerms, viewModel::toggleTerms)
            ErrorBanner(state.errorMessage)
            VSpace(4.dp)
            SolidButton(
                text = if (state.isRegisterMode) "Crear cuenta" else "Entrar a mi cuenta",
                onClick = viewModel::submit,
                background = CtaNavy,
                height = 48.dp,
                radius = 12.dp,
                family = Poppins,
                fontSize = 14,
                weight = FontWeight.SemiBold,
                trailingIcon = R.drawable.lucide_ic_arrow_right,
                elevation = 10.dp,
                loading = state.isLoading,
            )
        }
        VSpace(if (state.isRegisterMode) 12.dp else 60.dp)
    }
}

@Composable
private fun ModeSwitch(register: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Periwinkle)
            .border(1.dp, SkyTint.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(4.dp),
    ) {
        listOf(false to "Iniciar sesión", true to "Registrarme").forEach { (mode, label) ->
            val selected = mode == register
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .then(
                        if (selected) {
                            Modifier
                                .shadow(1.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(White)
                        } else {
                            Modifier
                        },
                    ).tap { onChange(mode) },
                contentAlignment = Alignment.Center,
            ) {
                Txt(
                    label,
                    12,
                    weight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) DeepNavy else Slate500,
                )
            }
        }
    }
}

@Composable
private fun RoleCard(
    letter: String,
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .then(
                if (selected) {
                    Modifier.shadow(8.dp, shape, ambientColor = Color(0x240013C1), spotColor = Color(0x240013C1))
                } else {
                    Modifier
                },
            ).clip(shape)
            .background(if (selected) Periwinkle else White)
            .border(if (selected) 2.dp else 1.dp, if (selected) Electric else Slate200, shape)
            .tap(onClick)
            .padding(16.dp),
    ) {
        Box(
            Modifier.size(32.dp).clip(RoundedCornerShape(12.dp)).background(if (selected) Lavender else Periwinkle),
            contentAlignment = Alignment.Center,
        ) {
            Txt(letter, 14, weight = FontWeight.Bold, color = if (selected) Electric else NavyBlue)
        }
        VSpace(8.dp)
        Txt(
            title,
            12,
            weight = FontWeight.SemiBold,
            color = if (selected) DeepNavy else Slate700,
            family = Poppins,
            letterSpacing = (-0.3).sp,
        )
        VSpace(2.dp)
        Txt(
            description,
            10,
            color = if (selected) Slate500 else Slate400,
            family = OpenSansCondensed,
            maxLines = 1,
        )
    }
}

@Composable
private fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Txt(text, 12, weight = FontWeight.Bold, color = DeepNavy, letterSpacing = (-0.3).sp, modifier = modifier)
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    background: Color = MintWhite,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(label)
        AuthInput(value, onValueChange, placeholder, keyboardType, background = background)
    }
}

@Composable
private fun AuthInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    visual: VisualTransformation = VisualTransformation.None,
    background: Color = MintWhite,
    trailing: @Composable () -> Unit = {},
) {
    val shape = RoundedCornerShape(12.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        visualTransformation = visual,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        cursorBrush = SolidColor(Electric),
        textStyle = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = DeepNavy),
        decorationBox = { inner ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(shape)
                    .background(background)
                    .border(1.dp, SkyTint, shape)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Txt(placeholder, 12, weight = FontWeight.Medium, color = Slate500)
                    }
                    inner()
                }
                trailing()
            }
        },
    )
}

@Composable
private fun TermsRow(checked: Boolean, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth().tap(onToggle), verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (checked) Electric else White)
                .border(1.5.dp, if (checked) Electric else DeepNavy, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) LIcon(R.drawable.lucide_ic_check, White, 12.dp)
        }
        HSpace(10.dp)
        androidx.compose.material3.Text(
            buildAnnotatedString {
                append("Acepto los ")
                withStyle(SpanStyle(color = Electric, fontWeight = FontWeight.SemiBold)) { append("Términos de Servicio") }
                append(" y la ")
                withStyle(SpanStyle(color = Electric, fontWeight = FontWeight.SemiBold)) { append("Política de Privacidad") }
                append(" de OptiFlow.")
            },
            style = TextStyle(fontFamily = Jakarta, fontSize = 11.sp, color = Slate500, lineHeight = 16.sp),
        )
    }
}
