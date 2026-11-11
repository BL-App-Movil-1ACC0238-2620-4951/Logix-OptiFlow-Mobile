package com.logix.optiflow.domain.repository.notification

import com.logix.optiflow.domain.model.notification.LoyaltyAccount
import java.util.UUID

interface LoyaltyAccountRepository {
    suspend fun getLoyaltyAccount(patientId: UUID): Result<LoyaltyAccount>
}
