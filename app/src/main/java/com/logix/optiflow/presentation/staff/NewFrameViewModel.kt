package com.logix.optiflow.presentation.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.inventory.Supplier
import com.logix.optiflow.domain.usecase.inventory.AddFrameModelUseCase
import com.logix.optiflow.domain.usecase.inventory.GetSuppliersUseCase
import com.logix.optiflow.domain.usecase.inventory.InitialStock
import com.logix.optiflow.domain.usecase.inventory.InitialStockFailed
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewFrameUiState(
    val sku: String = "",
    val name: String = "",
    val material: String = "",
    val shape: String = "",
    val color: String = "",
    val price: String = "",
    /** Solo el admin puede reponer; el asesor crea la montura sin stock. */
    val withStock: Boolean = false,
    val suppliers: List<Supplier> = emptyList(),
    val supplierId: UUID? = null,
    val quantity: String = "",
    val minimum: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    /** SKU creado; la pantalla abre su detalle. */
    val createdSku: String? = null,
) {
    val supplier: Supplier? get() = suppliers.firstOrNull { it.id == supplierId }
}

/** Alta de una montura en el catálogo (`POST /inventory/frames`) y, para el admin, su stock inicial. */
@HiltViewModel
class NewFrameViewModel @Inject constructor(
    private val addFrame: AddFrameModelUseCase,
    private val getSuppliers: GetSuppliersUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewFrameUiState())
    val uiState: StateFlow<NewFrameUiState> = _uiState.asStateFlow()

    private var storeId: UUID? = null
    private var loaded = false

    fun load(storeId: UUID?, canReplenish: Boolean) {
        this.storeId = storeId
        if (!canReplenish || loaded) return
        loaded = true
        _uiState.update { it.copy(withStock = true) }
        viewModelScope.launch {
            getSuppliers().onSuccess { list -> _uiState.update { it.copy(suppliers = list, supplierId = list.firstOrNull()?.id) } }
        }
    }

    fun setSku(v: String) = _uiState.update { it.copy(sku = v.uppercase(Locale.ROOT).filter { c -> c.isLetterOrDigit() || c == '-' }.take(100)) }

    fun setName(v: String) = _uiState.update { it.copy(name = v.take(255)) }

    fun setMaterial(v: String) = _uiState.update { it.copy(material = v.take(100)) }

    fun setShape(v: String) = _uiState.update { it.copy(shape = v.take(100)) }

    fun setColor(v: String) = _uiState.update { it.copy(color = v.take(100)) }

    fun setPrice(v: String) = _uiState.update { it.copy(price = v.filter { c -> c.isDigit() || c == '.' }.take(11)) }

    fun setQuantity(v: String) = _uiState.update { it.copy(quantity = v.filter(Char::isDigit).take(4)) }

    fun setMinimum(v: String) = _uiState.update { it.copy(minimum = v.filter(Char::isDigit).take(4)) }

    /** Pasa al siguiente proveedor de la lista. */
    fun nextSupplier() =
        _uiState.update { s ->
            if (s.suppliers.isEmpty()) return@update s
            val index = s.suppliers.indexOfFirst { it.id == s.supplierId }
            s.copy(supplierId = s.suppliers[(index + 1) % s.suppliers.size].id)
        }

    fun submit() {
        val s = _uiState.value
        if (s.isSaving) return
        val price = s.price.toDoubleOrNull()
        val error =
            when {
                s.sku.isBlank() -> "Escribe el SKU de la montura."
                s.name.isBlank() -> "Escribe el nombre de la montura."
                price == null || price <= 0.0 -> "Escribe un precio mayor que cero."
                else -> null
            }
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }
        val units = s.quantity.toIntOrNull() ?: 0
        val store = storeId
        if (s.withStock && units > MAX_REPLENISHMENT) {
            _uiState.update { it.copy(errorMessage = "Se pueden ingresar hasta $MAX_REPLENISHMENT unidades.") }
            return
        }
        if (s.withStock && units > 0 && (s.supplierId == null || store == null)) {
            _uiState.update { it.copy(errorMessage = "Elige el proveedor de las unidades.") }
            return
        }
        val initial =
            if (s.withStock && units > 0 && store != null && s.supplierId != null) {
                InitialStock(store, s.supplierId, units, s.minimum.toIntOrNull())
            } else {
                null
            }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            addFrame(s.sku.trim(), s.name.trim(), s.material.blankToNull(), s.shape.blankToNull(), s.color.blankToNull(), price!!, initial)
                .onSuccess { frame -> _uiState.update { it.copy(isSaving = false, createdSku = frame.sku) } }
                .onFailure { e ->
                    if (e is InitialStockFailed) {
                        // La montura ya existe; se abre su detalle y el stock se ingresa desde ahí.
                        val reason = e.cause?.toUserMessage().orEmpty()
                        _uiState.update {
                            it.copy(isSaving = false, createdSku = e.frame.sku, errorMessage = "La montura se creó, pero no se ingresó su stock. $reason".trim())
                        }
                    } else {
                        _uiState.update { it.copy(isSaving = false, errorMessage = e.toUserMessage()) }
                    }
                }
        }
    }

    private fun String.blankToNull(): String? = trim().takeIf { it.isNotEmpty() }
}
