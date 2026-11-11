package com.logix.optiflow.data.local.cache

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Última respuesta descargada de una consulta (en JSON), para mostrarla sin conexión. */
@Entity(tableName = "cached_responses")
data class CachedResponseEntity(
    @PrimaryKey val key: String,
    val json: String,
    val savedAtMillis: Long,
)
