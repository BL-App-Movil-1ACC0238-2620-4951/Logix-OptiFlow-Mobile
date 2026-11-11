package com.logix.optiflow.presentation.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.searchbooking.StaffMember
import com.logix.optiflow.domain.model.searchbooking.StaffRole
import com.logix.optiflow.domain.usecase.searchbooking.AssignStaffRoleUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetStaffMembersUseCase
import com.logix.optiflow.domain.usecase.searchbooking.RegisterStaffMemberUseCase
import com.logix.optiflow.domain.usecase.searchbooking.UpdateStaffMemberStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

fun roleLabel(role: String): String =
    when (StaffRole.from(role)) {
        StaffRole.OPTOMETRIST -> "Optometrista"
        StaffRole.SALES_ADVISOR -> "Asesor de ventas"
        StaffRole.TECHNICIAN -> "Técnico de laboratorio"
        StaffRole.ADMIN -> "Administrador"
        null -> role
    }

private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")


data class StaffMembersUiState(
    val isLoading: Boolean = true,
    val members: List<StaffMember> = emptyList(),
    val showForm: Boolean = false,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val role: StaffRole = StaffRole.OPTOMETRIST,
    /** Empleado cuyo rol se está cambiando. */
    val editingId: UUID? = null,
    val isSaving: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
)

/** Personal de la óptica del admin: alta, cambio de rol y activación (`/staff-members`). */
@HiltViewModel
class StaffMembersViewModel @Inject constructor(
    private val getMembers: GetStaffMembersUseCase,
    private val registerMember: RegisterStaffMemberUseCase,
    private val assignRole: AssignStaffRoleUseCase,
    private val updateStatus: UpdateStaffMemberStatusUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffMembersUiState())
    val uiState: StateFlow<StaffMembersUiState> = _uiState.asStateFlow()

    private var storeId: UUID? = null

    fun load(storeId: UUID?) {
        if (storeId == null) return
        this.storeId = storeId
        viewModelScope.launch {
            getMembers(storeId)
                .onSuccess { list -> _uiState.update { it.copy(isLoading = false, members = list) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.toUserMessage()) } }
        }
    }

    fun showForm(show: Boolean) =
        _uiState.update { it.copy(showForm = show, name = "", email = "", password = "", role = StaffRole.OPTOMETRIST, errorMessage = null, message = null) }

    fun setName(v: String) = _uiState.update { it.copy(name = v.take(120)) }

    fun setEmail(v: String) = _uiState.update { it.copy(email = v.trim().take(120)) }

    fun setPassword(v: String) = _uiState.update { it.copy(password = v.take(64)) }

    fun nextRole() = _uiState.update { it.copy(role = StaffRole.entries[(it.role.ordinal + 1) % StaffRole.entries.size]) }

    fun editRole(id: UUID?) = _uiState.update { it.copy(editingId = id, errorMessage = null, message = null) }

    fun register() {
        val s = _uiState.value
        val store = storeId ?: return
        val error =
            when {
                s.name.isBlank() -> "Escribe el nombre."
                !EMAIL.matches(s.email) -> "Escribe un correo válido."
                s.password.length < 8 -> "La contraseña temporal debe tener al menos 8 caracteres."
                else -> null
            }
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }
        save("Se registró a ${s.name.trim()} como ${roleLabel(s.role.name).lowercase()}.", closeForm = true) {
            registerMember(s.name.trim(), s.email, s.password, store, s.role.name)
        }
    }

    fun changeRole(member: StaffMember, role: StaffRole) =
        save("${member.name} ahora es ${roleLabel(role.name).lowercase()}; rige desde su siguiente inicio de sesión.") {
            assignRole(member.id, role.name)
        }

    fun toggleActive(member: StaffMember) =
        save(if (member.active) "${member.name} ya no puede iniciar sesión." else "${member.name} puede volver a iniciar sesión.") {
            updateStatus(member.id, !member.active)
        }

    private fun save(done: String, closeForm: Boolean = false, call: suspend () -> Result<StaffMember>) {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, message = null) }
            call()
                .onSuccess { updated ->
                    _uiState.update { s ->
                        val others = s.members.filterNot { it.id == updated.id }
                        s.copy(
                            isSaving = false,
                            members = (others + updated).sortedBy { it.name.lowercase() },
                            message = done,
                            editingId = null,
                            showForm = if (closeForm) false else s.showForm,
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isSaving = false, errorMessage = e.toUserMessage()) } }
        }
    }
}
