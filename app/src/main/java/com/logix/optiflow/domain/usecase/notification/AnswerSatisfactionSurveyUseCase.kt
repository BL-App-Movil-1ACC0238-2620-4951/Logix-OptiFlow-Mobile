package com.logix.optiflow.domain.usecase.notification

import com.logix.optiflow.domain.repository.notification.NotificationRepository
import java.util.UUID
import javax.inject.Inject

class AnswerSatisfactionSurveyUseCase @Inject constructor(
    private val repository: NotificationRepository,
) {
    /** Responde la encuesta (de 1 a 5); el backend también la marca como leída. */
    suspend operator fun invoke(notificationId: UUID, rating: Int, comment: String?): Result<Unit> =
        repository.answerSurvey(notificationId, rating, comment)
}
