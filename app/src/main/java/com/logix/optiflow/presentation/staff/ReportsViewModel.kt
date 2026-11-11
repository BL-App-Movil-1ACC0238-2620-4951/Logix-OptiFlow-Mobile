package com.logix.optiflow.presentation.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logix.optiflow.data.remote.toUserMessage
import com.logix.optiflow.domain.model.STORE_ZONE
import com.logix.optiflow.domain.model.report.ReportKind
import com.logix.optiflow.domain.usecase.report.ExportReportUseCase
import com.logix.optiflow.domain.usecase.report.GetMonthlyReportUseCase
import com.logix.optiflow.domain.usecase.report.MonthlyReport
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportsUiState(
    val isLoading: Boolean = true,
    val month: YearMonth = YearMonth.now(STORE_ZONE),
    val report: MonthlyReport? = null,
    val exporting: ReportKind? = null,
    val message: String? = null,
    val errorMessage: String? = null,
)

/** Dashboard del mes y exportación en CSV (`/reports/sales`, `/reports/production`, `/reports/inventory`). */
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val getReport: GetMonthlyReportUseCase,
    private val exportReport: ExportReportUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private var storeId: UUID? = null

    fun load(storeId: UUID?) {
        this.storeId = storeId
        val month = _uiState.value.month
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getReport(month, storeId)
                .onSuccess { report -> _uiState.update { if (it.month == month) it.copy(isLoading = false, report = report) else it } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.toUserMessage()) } }
        }
    }

    fun changeMonth(delta: Long) {
        val next = _uiState.value.month.plusMonths(delta)
        if (next.isAfter(YearMonth.now(STORE_ZONE))) return
        _uiState.update { it.copy(month = next, message = null) }
        load(storeId)
    }

    /** Pide el CSV; la pantalla lo guarda donde el usuario elija. */
    fun export(kind: ReportKind, onReady: (String) -> Unit) {
        if (_uiState.value.exporting != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(exporting = kind, message = null, errorMessage = null) }
            exportReport(kind, _uiState.value.month, storeId)
                .onSuccess { csv ->
                    _uiState.update { it.copy(exporting = null) }
                    onReady(csv)
                }
                .onFailure { e -> _uiState.update { it.copy(exporting = null, errorMessage = e.toUserMessage()) } }
        }
    }

    fun saved(ok: Boolean) =
        _uiState.update { it.copy(message = if (ok) "Reporte guardado." else null, errorMessage = if (ok) null else "No se pudo guardar el archivo.") }
}
