package com.logix.optiflow.domain.usecase.notification

import com.logix.optiflow.domain.model.notification.LoyaltyAccount
import com.logix.optiflow.domain.repository.notification.LoyaltyAccountRepository
import java.util.UUID
import javax.inject.Inject

class GetLoyaltyAccountUseCase @Inject constructor(
    private val repository: LoyaltyAccountRepository,
) {
    suspend operator fun invoke(patientId: UUID): Result<LoyaltyAccount> =
        repository.getLoyaltyAccount(patientId)
}
