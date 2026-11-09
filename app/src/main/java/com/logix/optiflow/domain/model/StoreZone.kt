package com.logix.optiflow.domain.model

import java.time.ZoneId

/** Las ópticas atienden en Lima: fechas y horas se muestran en esta zona aunque el teléfono tenga otra. */
val STORE_ZONE: ZoneId = ZoneId.of("America/Lima")
