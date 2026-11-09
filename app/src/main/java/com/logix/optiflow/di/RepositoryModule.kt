package com.logix.optiflow.di

import com.logix.optiflow.data.repository.clinical.ClinicalRecordRepositoryImpl
import com.logix.optiflow.data.repository.notification.NotificationRepositoryImpl
import com.logix.optiflow.data.repository.production.WorkOrderRepositoryImpl
import com.logix.optiflow.data.repository.searchbooking.AppointmentRepositoryImpl
import com.logix.optiflow.data.repository.searchbooking.OpticalStoreRepositoryImpl
import com.logix.optiflow.data.repository.searchbooking.PatientRepositoryImpl
import com.logix.optiflow.data.repository.searchbooking.StaffMemberRepositoryImpl
import com.logix.optiflow.domain.repository.clinical.ClinicalRecordRepository
import com.logix.optiflow.domain.repository.notification.NotificationRepository
import com.logix.optiflow.domain.repository.production.WorkOrderRepository
import com.logix.optiflow.domain.repository.searchbooking.AppointmentRepository
import com.logix.optiflow.domain.repository.searchbooking.OpticalStoreRepository
import com.logix.optiflow.domain.repository.searchbooking.PatientRepository
import com.logix.optiflow.domain.repository.searchbooking.StaffMemberRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindOpticalStoreRepository(impl: OpticalStoreRepositoryImpl): OpticalStoreRepository

    @Binds
    abstract fun bindPatientRepository(impl: PatientRepositoryImpl): PatientRepository

    @Binds
    abstract fun bindAppointmentRepository(impl: AppointmentRepositoryImpl): AppointmentRepository

    @Binds
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    abstract fun bindStaffMemberRepository(impl: StaffMemberRepositoryImpl): StaffMemberRepository

    @Binds
    abstract fun bindWorkOrderRepository(impl: WorkOrderRepositoryImpl): WorkOrderRepository

    @Binds
    abstract fun bindClinicalRecordRepository(impl: ClinicalRecordRepositoryImpl): ClinicalRecordRepository
}
