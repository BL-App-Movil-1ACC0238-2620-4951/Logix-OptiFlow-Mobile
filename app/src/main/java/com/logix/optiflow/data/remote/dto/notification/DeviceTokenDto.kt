package com.logix.optiflow.data.remote.dto.notification

/** Token de Firebase Cloud Messaging del celular del paciente. */
data class RegisterDeviceTokenRequest(
    val token: String,
    val platform: String = "ANDROID",
)
