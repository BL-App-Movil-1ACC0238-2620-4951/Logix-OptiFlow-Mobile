package com.logix.optiflow.data.remote.api.notification

import com.logix.optiflow.data.remote.dto.notification.AnswerSatisfactionSurveyRequest
import com.logix.optiflow.data.remote.dto.notification.LoyaltyAccountDto
import com.logix.optiflow.data.remote.dto.notification.NotificationDto
import com.logix.optiflow.data.remote.dto.notification.RegisterDeviceTokenRequest
import com.logix.optiflow.data.remote.dto.notification.UpdateNotificationRequest
import java.util.UUID
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationApi {

    @GET("patients/{patientId}/loyalty-account")
    suspend fun getLoyaltyAccount(
        @Path("patientId") patientId: UUID,
    ): LoyaltyAccountDto

    @GET("patients/{patientId}/notifications")
    suspend fun getPatientNotifications(
        @Path("patientId") patientId: UUID,
    ): List<NotificationDto>

    @PATCH("notifications/{id}")
    suspend fun markNotificationRead(
        @Path("id") notificationId: UUID,
        @Body request: UpdateNotificationRequest = UpdateNotificationRequest(read = true),
    ): NotificationDto

    @POST("patients/{patientId}/device-tokens")
    suspend fun registerDeviceToken(
        @Path("patientId") patientId: UUID,
        @Body request: RegisterDeviceTokenRequest,
    )

    @POST("notifications/{id}/survey-response")
    suspend fun answerSatisfactionSurvey(
        @Path("id") notificationId: UUID,
        @Body request: AnswerSatisfactionSurveyRequest,
    ): NotificationDto
}
