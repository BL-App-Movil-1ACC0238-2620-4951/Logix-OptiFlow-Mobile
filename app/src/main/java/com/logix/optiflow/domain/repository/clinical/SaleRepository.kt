package com.logix.optiflow.domain.repository.clinical

import com.logix.optiflow.domain.model.clinical.Sale
import java.util.UUID

interface SaleRepository {
    suspend fun getPatientSales(patientId: UUID): Result<List<Sale>>

    /** Registra la venta de una cotización aprobada; empieza pendiente de pago. */
    suspend fun register(quotationId: UUID): Result<Sale>

    suspend fun getSale(saleId: UUID): Result<Sale>

    /** Pago en efectivo en la óptica; el monto debe ser el total de la cotización. */
    suspend fun recordCashPayment(saleId: UUID, amount: Double): Result<Sale>

    /** Cierra una venta pagada: emite la boleta y el backend genera la orden de trabajo. */
    suspend fun close(saleId: UUID): Result<Sale>

    /** Link de Checkout Pro de Mercado Pago para que el paciente pague con tarjeta o Yape. */
    suspend fun createPaymentLink(saleId: UUID): Result<String>
}
