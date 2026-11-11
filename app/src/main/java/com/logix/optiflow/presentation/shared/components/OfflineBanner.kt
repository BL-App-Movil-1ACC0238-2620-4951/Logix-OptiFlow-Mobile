package com.logix.optiflow.presentation.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.logix.optiflow.domain.model.STORE_ZONE
import com.logix.optiflow.presentation.shared.OfflineViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SAVED_AT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.forLanguageTag("es-PE")).withZone(STORE_ZONE)

/** Aviso de que no hay conexión y se muestran los datos guardados, con la fecha de su última actualización. */
@Composable
fun OfflineBanner() {
    val vm: OfflineViewModel = hiltViewModel()
    val savedAt by vm.savedDataFrom.collectAsState()
    val date = savedAt ?: return
    Txt(
        "Sin conexión · datos actualizados el ${SAVED_AT.format(date).replace(".", "")}",
        12,
        weight = FontWeight.SemiBold,
        color = Color.White,
        align = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().background(Color(0xFF475569)).padding(horizontal = 16.dp, vertical = 6.dp),
    )
}
