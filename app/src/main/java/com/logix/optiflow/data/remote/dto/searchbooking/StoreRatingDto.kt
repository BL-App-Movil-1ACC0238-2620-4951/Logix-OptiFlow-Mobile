package com.logix.optiflow.data.remote.dto.searchbooking

import java.util.UUID

data class SaveFavoriteStoreRequest(
    val opticalStoreId: UUID,
)

data class RateOpticalStoreRequest(
    val patientId: UUID,
    val score: Double,
    val comment: String?,
)

data class StoreRatingDto(
    val patientId: UUID,
    val opticalStoreId: UUID,
    val score: Double,
    val averageRating: Double?,
)
