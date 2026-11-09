package com.logix.optiflow.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.logix.optiflow.data.remote.ApiClient
import com.logix.optiflow.di.SearchBookingModule
import com.logix.optiflow.ui.auth.AuthViewModel
import com.logix.optiflow.ui.booking.BookingViewModel
import com.logix.optiflow.ui.notifications.NotificationsViewModel
import com.logix.optiflow.ui.patient.PatientViewModel
import com.logix.optiflow.ui.search.StoreSearchViewModel
import com.logix.optiflow.ui.staff.StaffViewModel
import java.util.UUID

object ViewModelFactories {

    val storeSearch: ViewModelProvider.Factory =
        simpleFactory {
            StoreSearchViewModel(
                getOpticalStores = SearchBookingModule.getOpticalStoresUseCase,
                searchOpticalStores = SearchBookingModule.searchOpticalStoresUseCase,
            )
        }

    val notifications: ViewModelProvider.Factory =
        simpleFactory {
            NotificationsViewModel(
                getPatientSession = SearchBookingModule.getPatientSessionUseCase,
                getNotifications = SearchBookingModule.getNotificationsUseCase,
                notificationRepository = SearchBookingModule.notificationRepository,
            )
        }

    val auth: ViewModelProvider.Factory =
        simpleFactory {
            AuthViewModel(
                registerPatient = SearchBookingModule.registerPatientUseCase,
                login = SearchBookingModule.loginUseCase,
                getPatientSession = SearchBookingModule.getPatientSessionUseCase,
            )
        }

    val patient: ViewModelProvider.Factory =
        simpleFactory {
            PatientViewModel(
                patientRepository = SearchBookingModule.patientRepository,
                getPatientAppointments = SearchBookingModule.getPatientAppointmentsUseCase,
                getOpticalStores = SearchBookingModule.getOpticalStoresUseCase,
                api = ApiClient.searchBookingApi,
            )
        }

    val staff: ViewModelProvider.Factory =
        simpleFactory { StaffViewModel(api = ApiClient.searchBookingApi) }

    fun booking(storeId: UUID?): ViewModelProvider.Factory =
        simpleFactory {
            BookingViewModel(
                preselectedStoreId = storeId,
                getOpticalStores = SearchBookingModule.getOpticalStoresUseCase,
                getAvailability = SearchBookingModule.getStoreAvailabilityUseCase,
                bookAppointment = SearchBookingModule.bookAppointmentUseCase,
                getPatientSession = SearchBookingModule.getPatientSessionUseCase,
            )
        }

    private inline fun <reified T : ViewModel> simpleFactory(
        crossinline create: () -> T,
    ): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
        }
}
