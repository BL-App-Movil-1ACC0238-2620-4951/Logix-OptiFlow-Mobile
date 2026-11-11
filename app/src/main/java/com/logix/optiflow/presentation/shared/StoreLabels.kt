package com.logix.optiflow.presentation.shared

import com.logix.optiflow.domain.model.STORE_ZONE
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val es = Locale.forLanguageTag("es-PE")
private val dayFormat = DateTimeFormatter.ofPattern("EEE d", es)
private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

/** "Hoy", "Mañana" o "vie 14" para la fecha del turno en la hora de Lima. */
fun slotDayLabel(instant: Instant): String {
    val date = instant.atZone(STORE_ZONE).toLocalDate()
    val today = LocalDate.now(STORE_ZONE)
    return when (date) {
        today -> "Hoy"
        today.plusDays(1) -> "Mañana"
        else -> dayFormat.format(date).replace(".", "")
    }
}

/** "Hoy · 10:30" para el primer turno libre de una óptica. */
fun firstSlotLabel(instant: Instant): String =
    "${slotDayLabel(instant)} · ${timeFormat.format(instant.atZone(STORE_ZONE))}"

/** "0,8 km" con coma decimal; desde 10 km sin decimales. */
fun distanceLabel(km: Double): String =
    String.format(Locale.forLanguageTag("es"), if (km < 10) "%.1f km" else "%.0f km", km)
