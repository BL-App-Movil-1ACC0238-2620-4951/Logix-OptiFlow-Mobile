package com.logix.optiflow.data.remote.dto

import java.time.Instant
import java.util.UUID

data class PatientDto(
    val id: UUID,
    val name: String,
    val email: String,
    val phone: String,
    val createdAt: Instant?,
)

data class RegisterPatientRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
)

data class LoginRequest(
    val email: String,
    val password: String,
)

data class LoginResponse(
    val token: String,
    val expiresAt: Instant?,
    val userId: UUID,
    val role: String,
)
