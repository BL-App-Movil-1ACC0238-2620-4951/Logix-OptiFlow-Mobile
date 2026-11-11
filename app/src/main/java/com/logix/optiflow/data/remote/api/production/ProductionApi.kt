package com.logix.optiflow.data.remote.api.production

import com.logix.optiflow.data.remote.dto.production.AssignTechnicianRequest
import com.logix.optiflow.data.remote.dto.production.CompleteLensesRequest
import com.logix.optiflow.data.remote.dto.production.NamedRefDto
import com.logix.optiflow.data.remote.dto.production.NotifyDelayRequest
import com.logix.optiflow.data.remote.dto.production.SendToLaboratoryRequest
import com.logix.optiflow.data.remote.dto.production.UpdateWorkOrderStatusRequest
import com.logix.optiflow.data.remote.dto.production.WorkOrderDto
import com.logix.optiflow.data.remote.dto.production.WorkOrderStatusDto
import java.util.UUID
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** Taller: seguimiento de la orden de trabajo (técnico y admin modifican; el resto solo lee). */
interface ProductionApi {

    @GET("work-orders/{id}")
    suspend fun getWorkOrder(
        @Path("id") workOrderId: UUID,
    ): WorkOrderDto

    @PATCH("work-orders/{id}/technician")
    suspend fun assignTechnician(
        @Path("id") workOrderId: UUID,
        @Body request: AssignTechnicianRequest,
    ): WorkOrderDto

    @PATCH("work-orders/{id}/laboratory")
    suspend fun sendToLaboratory(
        @Path("id") workOrderId: UUID,
        @Body request: SendToLaboratoryRequest,
    ): WorkOrderDto

    @PATCH("work-orders/{id}/status")
    suspend fun updateStatus(
        @Path("id") workOrderId: UUID,
        @Body request: UpdateWorkOrderStatusRequest,
    ): WorkOrderStatusDto

    @PATCH("work-orders/{id}/lenses")
    suspend fun completeLenses(
        @Path("id") workOrderId: UUID,
        @Body request: CompleteLensesRequest,
    ): WorkOrderDto

    @POST("work-orders/{id}/delays")
    suspend fun notifyDelay(
        @Path("id") workOrderId: UUID,
        @Body request: NotifyDelayRequest,
    ): WorkOrderDto

    @GET("technicians")
    suspend fun getTechnicians(): List<NamedRefDto>

    @GET("laboratories")
    suspend fun getLaboratories(): List<NamedRefDto>
}
