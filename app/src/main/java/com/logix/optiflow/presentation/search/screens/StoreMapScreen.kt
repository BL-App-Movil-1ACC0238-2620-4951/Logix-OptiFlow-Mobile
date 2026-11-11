package com.logix.optiflow.presentation.search.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.logix.optiflow.R
import com.logix.optiflow.presentation.navigation.Routes
import com.logix.optiflow.presentation.patient.components.PatientScreen
import com.logix.optiflow.presentation.search.StoreSearchViewModel
import com.logix.optiflow.presentation.search.components.LOCATION_PERMISSIONS
import com.logix.optiflow.presentation.search.components.StoreMap
import com.logix.optiflow.presentation.search.components.hasLocationPermission
import com.logix.optiflow.presentation.shared.components.ErrorBanner
import com.logix.optiflow.presentation.shared.components.PatientLightTopBar
import com.logix.optiflow.presentation.shared.components.PatientTab
import com.logix.optiflow.presentation.shared.components.SolidButton
import com.logix.optiflow.presentation.shared.components.Txt
import com.logix.optiflow.presentation.shared.components.card
import com.logix.optiflow.presentation.shared.distanceLabel
import com.logix.optiflow.presentation.shared.firstSlotLabel
import com.logix.optiflow.presentation.theme.DeepNavy
import com.logix.optiflow.presentation.theme.Slate500

/** Mapa a pantalla completa con todas las ópticas; al tocar un pin se puede reservar ahí. */
@Composable
fun StoreMapScreen(nav: NavController) {
    val viewModel: StoreSearchViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = state.visibleStores.firstOrNull { it.id.toString() == selectedId }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        viewModel.refreshLocation()
    }
    LaunchedEffect(Unit) {
        if (hasLocationPermission(context)) viewModel.refreshLocation() else permission.launch(LOCATION_PERMISSIONS)
    }

    PatientScreen(
        nav = nav,
        tab = PatientTab.SEARCH,
        topBar = { PatientLightTopBar("Mapa de ópticas", onBack = { nav.popBackStack() }) },
    ) {
        Box(Modifier.fillMaxSize()) {
            StoreMap(
                stores = state.visibleStores,
                userLocation = state.userLocation,
                modifier = Modifier.fillMaxSize(),
                selectedStoreId = selected?.id,
                onStoreClick = { selectedId = it.id.toString() },
            )
            Column(
                // Abajo queda espacio para el crédito de OpenStreetMap, que debe verse siempre.
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ErrorBanner(state.errorMessage)
                if (selected != null) {
                    Column(
                        Modifier.fillMaxWidth().card(radius = 16.dp, elevation = 4.dp).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Txt(selected.name, 16, weight = FontWeight.Bold, color = DeepNavy, maxLines = 1)
                        Txt(selected.address, 12, color = Slate500, maxLines = 2)
                        val details = listOfNotNull(
                            state.distanceKm(selected)?.let(::distanceLabel),
                            state.firstSlots[selected.id]?.let { "Primer turno: ${firstSlotLabel(it)}" } ?: "Sin turnos libres",
                        )
                        Txt(details.joinToString(" · "), 12, color = Slate500)
                        SolidButton(
                            text = "Reservar cita",
                            onClick = { nav.navigate(Routes.book(selected.id)) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            height = 44.dp,
                            fontSize = 14,
                            leadingIcon = R.drawable.lucide_ic_calendar_plus,
                        )
                    }
                } else {
                    Txt(
                        "Toca un pin para ver la óptica",
                        12,
                        weight = FontWeight.Bold,
                        color = DeepNavy,
                        modifier = Modifier.card(radius = 10.dp, elevation = 2.dp).padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
            }
        }
    }
}
