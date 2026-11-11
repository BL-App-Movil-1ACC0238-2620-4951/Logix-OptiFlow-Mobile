package com.logix.optiflow.data.remote.dto.notification

import java.time.Instant
import java.util.UUID

data class LoyaltyAccountDto(
    val patientId: UUID,
    val balance: Int,
    val lastBirthdayDiscountYear: Int?,
    val updatedAt: Instant?,
)
