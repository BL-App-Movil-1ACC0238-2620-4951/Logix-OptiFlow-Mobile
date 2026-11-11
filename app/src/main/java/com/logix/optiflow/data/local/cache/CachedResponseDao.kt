package com.logix.optiflow.data.local.cache

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface CachedResponseDao {

    @Query("SELECT * FROM cached_responses WHERE `key` = :key")
    suspend fun get(key: String): CachedResponseEntity?

    @Upsert
    suspend fun save(response: CachedResponseEntity)

    @Query("DELETE FROM cached_responses")
    suspend fun clear()
}
