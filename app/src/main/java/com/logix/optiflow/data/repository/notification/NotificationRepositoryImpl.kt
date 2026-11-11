package com.logix.optiflow.data.repository.notification

import com.logix.optiflow.data.mapper.notification.toDomain
import com.logix.optiflow.data.push.PushTokenProvider
import com.logix.optiflow.data.remote.api.notification.NotificationApi
import com.logix.optiflow.data.remote.dto.notification.AnswerSatisfactionSurveyRequest
import com.logix.optiflow.data.remote.dto.notification.NotificationDto
import com.logix.optiflow.data.remote.dto.notification.RegisterDeviceTokenRequest
import com.logix.optiflow.data.remote.safeApiCall
import com.logix.optiflow.data.repository.offline.OfflineDataRepositoryImpl
import com.logix.optiflow.domain.model.notification.NotificationItem
import com.logix.optiflow.domain.repository.notification.NotificationRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val api: NotificationApi,
    private val pushTokenProvider: PushTokenProvider,
    private val offline: OfflineDataRepositoryImpl,
) : NotificationRepository {

    override suspend fun loadForPatient(patientId: UUID): Result<List<NotificationItem>> =
        offline.list("notifications/$patientId", NotificationDto::class.java) { api.getPatientNotifications(patientId) }
            .map { dtos -> dtos.map { it.toDomain() } }

    override suspend fun markRead(notificationId: UUID): Result<Unit> =
        safeApiCall {
            try {
                api.markNotificationRead(notificationId)
            } catch (e: HttpException) {
                // 409: ya estaba leída, que es lo que se quería.
                if (e.code() != 409) throw e
            }
            Unit
        }

    override suspend fun answerSurvey(notificationId: UUID, rating: Int, comment: String?): Result<Unit> =
        safeApiCall {
            api.answerSatisfactionSurvey(
                notificationId,
                AnswerSatisfactionSurveyRequest(rating, comment?.trim()?.takeIf { it.isNotEmpty() }),
            )
            Unit
        }

    override suspend fun registerDevice(patientId: UUID): Result<Unit> =
        safeApiCall {
            api.registerDeviceToken(patientId, RegisterDeviceTokenRequest(pushTokenProvider.currentToken()))
        }
}
