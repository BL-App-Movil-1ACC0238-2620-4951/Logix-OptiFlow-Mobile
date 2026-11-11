package com.logix.optiflow.data.repository.notification

import com.logix.optiflow.data.mapper.notification.toDomain
import com.logix.optiflow.data.remote.api.notification.NotificationApi
import com.logix.optiflow.data.remote.safeApiCall
import com.logix.optiflow.domain.model.notification.LoyaltyAccount
import com.logix.optiflow.domain.repository.notification.LoyaltyAccountRepository
import java.util.UUID
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoyaltyAccountRepositoryImpl @Inject constructor(
    private val api: NotificationApi,
) : LoyaltyAccountRepository {

    override suspend fun getLoyaltyAccount(patientId: UUID): Result<LoyaltyAccount> =
        safeApiCall {
            try {
                api.getLoyaltyAccount(patientId).toDomain()
            } catch (e: HttpException) {
                // Sin cuenta de puntos todavía: el backend responde 404 y el saldo es 0.
                if (e.code() == 404) LoyaltyAccount(patientId, 0, null) else throw e
            }
        }
}
