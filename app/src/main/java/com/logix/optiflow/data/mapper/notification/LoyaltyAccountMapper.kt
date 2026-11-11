package com.logix.optiflow.data.mapper.notification

import com.logix.optiflow.data.remote.dto.notification.LoyaltyAccountDto
import com.logix.optiflow.domain.model.notification.LoyaltyAccount

fun LoyaltyAccountDto.toDomain(): LoyaltyAccount =
    LoyaltyAccount(
        patientId = patientId,
        balance = balance,
        lastBirthdayDiscountYear = lastBirthdayDiscountYear,
    )
