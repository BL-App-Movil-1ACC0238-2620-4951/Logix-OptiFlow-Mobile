package com.logix.optiflow.domain.usecase.report

import com.logix.optiflow.domain.model.report.ReportKind
import com.logix.optiflow.domain.repository.report.ReportRepository
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject

/** CSV de un reporte del mes, tal como lo exporta el backend. */
class ExportReportUseCase @Inject constructor(
    private val repository: ReportRepository,
) {
    suspend operator fun invoke(kind: ReportKind, month: YearMonth, opticalStoreId: UUID?): Result<String> =
        repository.getCsv(kind, month.atDay(1), month.atEndOfMonth(), opticalStoreId)
}
