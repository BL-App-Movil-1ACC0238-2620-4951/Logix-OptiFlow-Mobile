package com.logix.optiflow.presentation.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.inventory.Supplier
import com.logix.optiflow.domain.usecase.inventory.GetSuppliersUseCase
import com.logix.optiflow.domain.usecase.inventory.RegisterSupplierUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SuppliersUiState(
    val isLoading: Boolean = true,
    val suppliers: List<Supplier> = emptyList(),
    val name: String = "",
    val contact: String = "",
    val isSaving: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class SuppliersViewModel @Inject constructor(
    private val getSuppliers: GetSuppliersUseCase,
    private val registerSupplier: RegisterSupplierUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SuppliersUiState())
    val uiState: StateFlow<SuppliersUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getSuppliers()
                .onSuccess { list -> _uiState.update { it.copy(isLoading = false, suppliers = list) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.toUserMessage()) } }
        }
    }

    fun setName(v: String) = _uiState.update { it.copy(name = v.take(255)) }

    fun setContact(v: String) = _uiState.update { it.copy(contact = v.take(255)) }

    fun register() {
        val s = _uiState.value
        if (s.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Escribe el nombre del proveedor.") }
            return
        }
        if (s.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, message = null) }
            registerSupplier(s.name.trim(), s.contact.trim().ifBlank { null })
                .onSuccess { added ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            suppliers = (it.suppliers + added).sortedBy { x -> x.name.lowercase() },
                            name = "",
                            contact = "",
                            message = "Proveedor ${added.name} registrado.",
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isSaving = false, errorMessage = e.toUserMessage()) } }
        }
    }
}
