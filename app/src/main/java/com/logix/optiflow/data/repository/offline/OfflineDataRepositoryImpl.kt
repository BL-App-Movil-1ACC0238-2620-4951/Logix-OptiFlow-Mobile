package com.logix.optiflow.data.repository.offline

import com.logix.optiflow.data.local.cache.CachedResponseDao
import com.logix.optiflow.data.local.cache.CachedResponseEntity
import com.logix.optiflow.data.remote.safeApiCall
import com.logix.optiflow.domain.repository.offline.OfflineDataRepository
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.IOException
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Guarda en Room la última respuesta de cada consulta y, si no hay conexión, devuelve la guardada
 * y avisa desde qué fecha es. Solo se usa para lecturas: las operaciones que cambian datos van
 * directo al backend y fallan sin conexión.
 */
@Singleton
class OfflineDataRepositoryImpl @Inject constructor(
    private val dao: CachedResponseDao,
    private val moshi: Moshi,
) : OfflineDataRepository {

    private val savedDataFrom = MutableStateFlow<Instant?>(null)
    override val showingSavedDataFrom: StateFlow<Instant?> = savedDataFrom.asStateFlow()

    override suspend fun clear() {
        dao.clear()
        savedDataFrom.value = null
    }

    /** Descarga la lista con [fetch] y la guarda con [key]; sin conexión devuelve la última guardada. */
    suspend fun <T> list(key: String, type: Class<T>, fetch: suspend () -> List<T>): Result<List<T>> =
        cached(key, moshi.adapter(Types.newParameterizedType(List::class.java, type)), fetch)

    /** Igual que [list], para una consulta que devuelve un solo objeto. */
    suspend fun <T : Any> item(key: String, type: Class<T>, fetch: suspend () -> T): Result<T> =
        cached(key, moshi.adapter(type), fetch)

    private suspend fun <T> cached(key: String, adapter: JsonAdapter<T>, fetch: suspend () -> T): Result<T> {
        val result = safeApiCall {
            fetch().also { dao.save(CachedResponseEntity(key, adapter.toJson(it), System.currentTimeMillis())) }
        }
        if (result.isSuccess) {
            savedDataFrom.value = null
            return result
        }
        if (result.exceptionOrNull() !is IOException) return result
        val saved = safeApiCall { dao.get(key)?.let { it to adapter.fromJson(it.json) } }.getOrNull()
        val value = saved?.second ?: return result
        savedDataFrom.value = Instant.ofEpochMilli(saved.first.savedAtMillis)
        return Result.success(value)
    }
}
