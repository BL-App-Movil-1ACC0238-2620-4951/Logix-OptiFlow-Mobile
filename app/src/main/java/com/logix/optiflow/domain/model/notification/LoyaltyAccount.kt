package com.logix.optiflow.domain.model.notification

import java.util.UUID

data class LoyaltyAccount(
    val patientId: UUID,
    val balance: Int,
    val lastBirthdayDiscountYear: Int?,
)
