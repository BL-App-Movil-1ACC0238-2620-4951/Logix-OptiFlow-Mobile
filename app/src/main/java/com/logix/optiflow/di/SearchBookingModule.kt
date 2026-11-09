package com.logix.optiflow.di

import android.content.Context
import com.logix.optiflow.data.local.PatientSessionStore
import com.logix.optiflow.data.local.notification.NotificationReadStore
import com.logix.optiflow.data.remote.ApiClient
import com.logix.optiflow.data.repository.notification.NotificationRepositoryImpl
import com.logix.optiflow.data.repository.searchbooking.AppointmentRepositoryImpl
import com.logix.optiflow.data.repository.searchbooking.OpticalStoreRepositoryImpl
import com.logix.optiflow.data.repository.searchbooking.PatientRepositoryImpl
import com.logix.optiflow.domain.repository.notification.NotificationRepository
import com.logix.optiflow.domain.repository.searchbooking.AppointmentRepository
import com.logix.optiflow.domain.repository.searchbooking.OpticalStoreRepository
import com.logix.optiflow.domain.repository.searchbooking.PatientRepository
import com.logix.optiflow.domain.usecase.notification.GetNotificationsUseCase
import com.logix.optiflow.domain.usecase.searchbooking.BookAppointmentUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetOpticalStoresUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetPatientAppointmentsUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetPatientSessionUseCase
import com.logix.optiflow.domain.usecase.searchbooking.GetStoreAvailabilityUseCase
import com.logix.optiflow.domain.usecase.searchbooking.LoginUseCase
import com.logix.optiflow.domain.usecase.searchbooking.RegisterPatientUseCase
import com.logix.optiflow.domain.usecase.searchbooking.SearchOpticalStoresUseCase

object SearchBookingModule {

    private lateinit var appContext: Context

    fun init(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context.applicationContext
        }
    }

    private fun requireContext(): Context {
        check(::appContext.isInitialized) {
            "SearchBookingModule.init(context) must be called first"
        }
        return appContext
    }

    val opticalStoreRepository: OpticalStoreRepository by lazy {
        OpticalStoreRepositoryImpl(api = ApiClient.searchBookingApi)
    }

    val getOpticalStoresUseCase: GetOpticalStoresUseCase by lazy {
        GetOpticalStoresUseCase(opticalStoreRepository)
    }

    val searchOpticalStoresUseCase: SearchOpticalStoresUseCase by lazy {
        SearchOpticalStoresUseCase(opticalStoreRepository)
    }

    private val patientSessionStore: PatientSessionStore by lazy {
        PatientSessionStore(requireContext())
    }

    val patientRepository: PatientRepository by lazy {
        PatientRepositoryImpl(
            api = ApiClient.searchBookingApi,
            sessionStore = patientSessionStore,
        )
    }

    val registerPatientUseCase: RegisterPatientUseCase by lazy {
        RegisterPatientUseCase(patientRepository)
    }

    val loginUseCase: LoginUseCase by lazy {
        LoginUseCase(patientRepository)
    }

    val getPatientSessionUseCase: GetPatientSessionUseCase by lazy {
        GetPatientSessionUseCase(patientRepository)
    }

    val appointmentRepository: AppointmentRepository by lazy {
        AppointmentRepositoryImpl(
            api = ApiClient.searchBookingApi,
            sessionStore = patientSessionStore,
        )
    }

    val getStoreAvailabilityUseCase: GetStoreAvailabilityUseCase by lazy {
        GetStoreAvailabilityUseCase(appointmentRepository)
    }

    val bookAppointmentUseCase: BookAppointmentUseCase by lazy {
        BookAppointmentUseCase(appointmentRepository)
    }

    val getPatientAppointmentsUseCase: GetPatientAppointmentsUseCase by lazy {
        GetPatientAppointmentsUseCase(appointmentRepository)
    }

    private val notificationReadStore: NotificationReadStore by lazy {
        NotificationReadStore(requireContext())
    }

    val notificationRepository: NotificationRepository by lazy {
        NotificationRepositoryImpl(
            api = ApiClient.searchBookingApi,
            readStore = notificationReadStore,
        )
    }

    val getNotificationsUseCase: GetNotificationsUseCase by lazy {
        GetNotificationsUseCase(notificationRepository)
    }
}
