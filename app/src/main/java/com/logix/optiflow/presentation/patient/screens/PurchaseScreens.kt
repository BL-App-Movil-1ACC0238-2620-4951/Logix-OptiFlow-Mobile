package com.logix.optiflow.presentation.patient.screens

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.presentation.patient.PatientAccountViewModel
import com.logix.optiflow.presentation.patient.PurchaseUi
import com.logix.optiflow.presentation.patient.components.PatientScreen
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.IconTile
import com.logix.optiflow.presentation.shared.components.PatientDarkTopBar
import com.logix.optiflow.presentation.shared.components.SolidButton
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.card
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.theme.DeepNavy
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.ErrorRed
import com.logix.optiflow.presentation.theme.Lavender
import com.logix.optiflow.presentation.theme.NavyBlue
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.Slate500
import com.logix.optiflow.presentation.theme.White

@Composable
fun PurchasesScreen(nav: NavController, vm: PatientAccountViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    // Al volver de Mercado Pago (optiflow://payment-result) se recargan las compras.
    var resumedOnce by remember { mutableStateOf(false) }
    LifecycleResumeEffect(Unit) {
        if (resumedOnce) vm.load() else resumedOnce = true
        onPauseOrDispose {}
    }
    LaunchedEffect(state.paymentUrl) {
        state.paymentUrl?.let { url ->
            vm.paymentOpened()
            CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, Uri.parse(url))
        }
    }

    PatientScreen(
        nav = nav,
        tab = null,
        topBar = { PatientDarkTopBar("Mis compras", onBack = { nav.popBackStack() }) },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            VSpace(24.dp)
            when {
                state.isLoading -> Txt("Cargando tus compras...", 14, color = Slate500)
                state.errorMessage != null -> {
                    Txt(state.errorMessage.orEmpty(), 14, color = Slate500)
                    VSpace(10.dp)
                    Txt("Reintentar", 14, weight = FontWeight.Bold, color = Electric, modifier = Modifier.tap { vm.load() })
                }
                state.purchases.isEmpty() ->
                    Column(Modifier.fillMaxWidth().padding(top = 100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        IconTile(R.drawable.lucide_ic_receipt, White, NavyBlue, size = 56.dp, iconSize = 22.dp, radius = 16.dp)
                        VSpace(14.dp)
                        Txt("Aún no tienes compras", 14, weight = FontWeight.Bold, color = Color(0xFF111827))
                        VSpace(4.dp)
                        Txt(
                            "Cuando compres tus lentes en una óptica, aquí verás tu boleta.",
                            12,
                            color = Slate400,
                            align = TextAlign.Center,
                        )
                    }
                else -> {
                    state.paymentError?.let {
                        Txt(it, 13, color = ErrorRed)
                        VSpace(12.dp)
                    }
                    state.purchases.forEach { purchase ->
                        PurchaseCard(purchase, paying = state.payingSaleId == purchase.id) { vm.pay(purchase.id) }
                        VSpace(12.dp)
                    }
                }
            }
            VSpace(16.dp)
        }
    }
}

@Composable
private fun PurchaseCard(purchase: PurchaseUi, paying: Boolean, onPay: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .card(radius = 16.dp, border = Lavender, elevation = 2.dp)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(R.drawable.lucide_ic_receipt, Lavender, Electric, size = 40.dp, iconSize = 18.dp, radius = 12.dp)
            HSpace(12.dp)
            Column(Modifier.weight(1f)) {
                Txt(purchase.receiptNumber?.let { "Boleta $it" } ?: "Compra sin boleta", 14, weight = FontWeight.Bold, color = DeepNavy)
                Txt("${purchase.date} · ${purchase.paymentMethod}", 12, color = Slate500)
            }
            Column(horizontalAlignment = Alignment.End) {
                Txt(purchase.amount, 15, weight = FontWeight.Bold, color = DeepNavy)
                Txt(purchase.status, 11, weight = FontWeight.SemiBold, color = Electric)
            }
        }
        if (purchase.paymentRejected) {
            VSpace(10.dp)
            Txt("Tu último pago fue rechazado. Puedes intentarlo de nuevo.", 12, color = ErrorRed)
        }
        if (purchase.payable) {
            VSpace(12.dp)
            SolidButton(
                text = if (paying) "Abriendo Mercado Pago…" else "Pagar con tarjeta o Yape",
                onClick = onPay,
                modifier = Modifier.fillMaxWidth(),
                height = 44.dp,
            )
        }
    }
}
