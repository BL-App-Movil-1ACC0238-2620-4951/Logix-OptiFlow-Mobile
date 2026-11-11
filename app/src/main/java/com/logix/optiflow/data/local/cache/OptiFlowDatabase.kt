package com.logix.optiflow.data.local.cache

import androidx.room.Database
import androidx.room.RoomDatabase

/** Base de datos local (Room): caché de solo lectura de lo último consultado. */
@Database(entities = [CachedResponseEntity::class], version = 1, exportSchema = false)
abstract class OptiFlowDatabase : RoomDatabase() {
    abstract fun cachedResponseDao(): CachedResponseDao
}
