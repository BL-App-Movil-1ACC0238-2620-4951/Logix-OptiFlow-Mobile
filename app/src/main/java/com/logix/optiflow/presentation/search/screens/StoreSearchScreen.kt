package com.logix.optiflow.presentation.search.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.domain.model.searchbooking.OpticalStore
import com.logix.optiflow.presentation.navigation.Routes
import com.logix.optiflow.presentation.patient.components.PatientScreen
import com.logix.optiflow.presentation.search.StoreSearchViewModel
import com.logix.optiflow.presentation.shared.components.BellButton
import com.logix.optiflow.presentation.shared.components.ErrorBanner
import com.logix.optiflow.presentation.shared.components.HSpace
import com.logix.optiflow.presentation.shared.components.IconTile
import com.logix.optiflow.presentation.shared.components.LIcon
import com.logix.optiflow.presentation.shared.components.PatientLightTopBar
import com.logix.optiflow.presentation.shared.components.PatientTab
import com.logix.optiflow.presentation.shared.components.Pill
import com.logix.optiflow.presentation.shared.components.SegmentChip
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.VSpace
import com.logix.optiflow.presentation.shared.components.card
import com.logix.optiflow.presentation.shared.components.tap
import com.logix.optiflow.presentation.theme.DeepNavy
import com.logix.optiflow.presentation.theme.Electric
import com.logix.optiflow.presentation.theme.InfoBanner
import com.logix.optiflow.presentation.theme.Jakarta
import com.logix.optiflow.presentation.theme.Lavender
import com.logix.optiflow.presentation.theme.MintWhite
import com.logix.optiflow.presentation.theme.NavyBlue
import com.logix.optiflow.presentation.theme.PatientHeader
import com.logix.optiflow.presentation.theme.SkyTint
import com.logix.optiflow.presentation.theme.Slate400
import com.logix.optiflow.presentation.theme.Slate500
import com.logix.optiflow.presentation.theme.White
import java.util.Locale
import kotlinx.coroutines.delay

private val chips = listOf("Cerca de mí", "Abierto ahora", "Primera hora")
private val distances = listOf("0,8 km", "1,4 km", "2,1 km", "3,2 km")
private val firstSlots = listOf("Hoy · 10:30", "Hoy · 12:00", "Mañana · 09:00", "Mañana · 11:30")

@Composable
fun StoreSearchScreen(nav: NavController) {
    val viewModel: StoreSearchViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    var chip by remember { mutableIntStateOf(0) }

    LaunchedEffect(state.nameQuery) {
        delay(400)
        viewModel.search()
    }

    val stores =
        when (chip) {
            1 -> state.stores.filter { it.status.uppercase(Locale.US) in setOf("ACTIVE", "OPEN", "ABIERTO") }
            2 -> state.stores.sortedByDescending { it.rating ?: 0.0 }
            else -> state.stores
        }

    PatientScreen(
        nav = nav,
        tab = PatientTab.SEARCH,
        topBar = {
            PatientLightTopBar("Buscar", onBack = { nav.popBackStack() }) {
                BellButton(onClick = { nav.navigate(Routes.P_NOTIFICATIONS) })
            }
        },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            VSpace(6.dp)
            SearchField(
                value = state.nameQuery,
                onValueChange = viewModel::onNameChange,
                placeholder = "Buscar armazón, dirección, estilo...",
                onSearch = viewModel::search,
            )
            VSpace(20.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(102.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(InfoBanner)
                    .border(1.dp, SkyTint, RoundedCornerShape(16.dp))
                    .tap { nav.navigate(Routes.P_CATALOG) }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(White),
                    contentAlignment = Alignment.Center,
                ) {
                    Txt("?", 24, color = DeepNavy)
                }
                HSpace(14.dp)
                Column(Modifier.weight(1f)) {
                    Txt("¿YA SABES QUÉ TE GUSTA?", 10, weight = FontWeight.Bold, color = Electric)
                    Txt("Busca por montura", 15, weight = FontWeight.Bold, color = DeepNavy)
                    Txt("Elige un modelo y descubre dónde está disponible.", 12, color = Slate500, lineHeight = 15.sp)
                }
                LIcon(R.drawable.lucide_ic_chevron_right, Slate500, 20.dp)
            }
            VSpace(22.dp)
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                chips.forEachIndexed { index, label ->
                    SegmentChip(
                        text = label,
                        selected = chip == index,
                        onClick = { chip = index },
                        selectedBg = DeepNavy,
                        selectedText = White,
                        unselectedText = DeepNavy,
                        border = SkyTint,
                        fontSize = 13,
                        height = 34.dp,
                        weight = FontWeight.Medium,
                    )
                }
            }
            VSpace(22.dp)
            MapPreview(count = stores.size)
            VSpace(28.dp)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Txt("Resultados cercanos", 17, weight = FontWeight.Bold, color = DeepNavy, modifier = Modifier.weight(1f))
                Txt("Mapa", 12, weight = FontWeight.Bold, color = Electric)
            }
            VSpace(14.dp)
            if (state.isLoading && stores.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Electric)
                }
            }
            ErrorBanner(state.errorMessage)
            state.infoMessage?.let { Txt(it, 12, color = Slate500) }
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                stores.forEachIndexed { index, store ->
                    StoreCard(
                        store = store,
                        index = index,
                        isFavorite = store.id in state.favoriteStoreIds,
                        onFavorite = { viewModel.saveFavorite(store.id) },
                    ) { nav.navigate(Routes.book(store.id)) }
                }
            }
            VSpace(24.dp)
        }
    }
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onSearch: () -> Unit = {},
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        cursorBrush = SolidColor(Electric),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        textStyle = TextStyle(fontFamily = Jakarta, fontSize = 14.sp, color = DeepNavy),
        decorationBox = { inner ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .shadow(2.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(MintWhite)
                    .border(1.dp, Color(0xFF8B9AC0), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LIcon(R.drawable.lucide_ic_search, PatientHeader, 20.dp)
                HSpace(10.dp)
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Txt(placeholder, 14, color = Slate400, maxLines = 1)
                    inner()
                }
            }
        },
    )
}

@Composable
private fun MapPreview(count: Int) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(144.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFD9E5FB))
            .border(1.dp, SkyTint, RoundedCornerShape(16.dp)),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val step = 34.dp.toPx()
            var x = -size.height
            while (x < size.width + size.height) {
                drawLine(
                    color = Color(0xFFE7EEFD),
                    start = Offset(x, size.height),
                    end = Offset(x + size.height, 0f),
                    strokeWidth = 14.dp.toPx(),
                )
                x += step
            }
        }
        MapPin(Modifier.offset(x = 82.dp, y = 32.dp))
        MapPin(Modifier.offset(x = 276.dp, y = 42.dp))
        MapPin(Modifier.offset(x = 171.dp, y = 90.dp))
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, bottom = 14.dp)
                .card(radius = 10.dp, elevation = 2.dp)
                .padding(horizontal = 12.dp, vertical = 7.dp),
        ) {
            Txt("$count ópticas cerca de ti", 12, weight = FontWeight.Bold, color = DeepNavy)
        }
    }
}

@Composable
private fun MapPin(modifier: Modifier) {
    Box(
        modifier
            .size(30.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(PatientHeader)
            .border(2.dp, White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        LIcon(R.drawable.lucide_ic_map_pin, White, 15.dp)
    }
}

@Composable
private fun StoreCard(
    store: OpticalStore,
    index: Int,
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .card(radius = 16.dp, border = SkyTint)
            .tap(onClick)
            .padding(start = 12.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(R.drawable.lucide_ic_eye, Lavender, NavyBlue, size = 64.dp, iconSize = 28.dp, radius = 12.dp)
        HSpace(12.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Pill(
                text = firstSlots[index % firstSlots.size],
                background = SkyTint,
                color = DeepNavy,
                icon = R.drawable.lucide_ic_clock,
                size = 12,
                padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            )
            Txt(store.name, 16, weight = FontWeight.Bold, color = DeepNavy, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                LIcon(R.drawable.lucide_ic_map_pin, NavyBlue, 14.dp)
                HSpace(5.dp)
                val rating = store.rating?.let { String.format(Locale("es"), "%.1f", it) } ?: "—"
                Txt("$rating · ${distances[index % distances.size]}", 12, color = Slate500)
            }
            Txt("Examen visual · Lentes · Monturas", 11, color = Slate400, maxLines = 1)
        }
        Box(Modifier.size(36.dp).clip(CircleShape).tap(onFavorite), contentAlignment = Alignment.Center) {
            LIcon(R.drawable.lucide_ic_heart, if (isFavorite) Electric else Slate400, 20.dp)
        }
        LIcon(R.drawable.lucide_ic_chevron_right, Slate500, 20.dp)
    }
}
