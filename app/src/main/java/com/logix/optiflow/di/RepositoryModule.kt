package com.logix.optiflow.di

import com.logix.optiflow.data.repository.clinical.ClinicalRecordRepositoryImpl
import com.logix.optiflow.data.repository.clinical.QuotationRepositoryImpl
import com.logix.optiflow.data.repository.clinical.SaleRepositoryImpl
import com.logix.optiflow.data.repository.inventory.FavoriteFrameRepositoryImpl
import com.logix.optiflow.data.repository.inventory.FrameModelRepositoryImpl
import com.logix.optiflow.data.repository.inventory.InventoryRepositoryImpl
import com.logix.optiflow.data.repository.report.ReportRepositoryImpl
import com.logix.optiflow.data.repository.notification.LoyaltyAccountRepositoryImpl
import com.logix.optiflow.data.repository.notification.NotificationRepositoryImpl
import com.logix.optiflow.data.repository.production.WorkOrderRepositoryImpl
import com.logix.optiflow.data.repository.searchbooking.AppointmentRepositoryImpl
import com.logix.optiflow.data.repository.searchbooking.LocationRepositoryImpl
import com.logix.optiflow.data.repository.searchbooking.OpticalStoreRepositoryImpl
import com.logix.optiflow.data.repository.searchbooking.PatientRepositoryImpl
import com.logix.optiflow.data.repository.searchbooking.StaffMemberRepositoryImpl
import com.logix.optiflow.domain.repository.clinical.ClinicalRecordRepository
import com.logix.optiflow.domain.repository.clinical.QuotationRepository
import com.logix.optiflow.domain.repository.clinical.SaleRepository
import com.logix.optiflow.domain.repository.inventory.FavoriteFrameRepository
import com.logix.optiflow.domain.repository.inventory.FrameModelRepository
import com.logix.optiflow.domain.repository.inventory.InventoryRepository
import com.logix.optiflow.domain.repository.report.ReportRepository
import com.logix.optiflow.domain.repository.notification.LoyaltyAccountRepository
import com.logix.optiflow.domain.repository.notification.NotificationRepository
import com.logix.optiflow.domain.repository.production.WorkOrderRepository
import com.logix.optiflow.domain.repository.searchbooking.AppointmentRepository
import com.logix.optiflow.domain.repository.searchbooking.LocationRepository
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
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    abstract fun bindStaffMemberRepository(impl: StaffMemberRepositoryImpl): StaffMemberRepository

    @Binds
    abstract fun bindWorkOrderRepository(impl: WorkOrderRepositoryImpl): WorkOrderRepository

    @Binds
    abstract fun bindClinicalRecordRepository(impl: ClinicalRecordRepositoryImpl): ClinicalRecordRepository

    @Binds
    abstract fun bindFrameModelRepository(impl: FrameModelRepositoryImpl): FrameModelRepository

    @Binds
    abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository

    @Binds
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds
    abstract fun bindFavoriteFrameRepository(impl: FavoriteFrameRepositoryImpl): FavoriteFrameRepository

    @Binds
    abstract fun bindLoyaltyAccountRepository(impl: LoyaltyAccountRepositoryImpl): LoyaltyAccountRepository

    @Binds
    abstract fun bindSaleRepository(impl: SaleRepositoryImpl): SaleRepository

    @Binds
    abstract fun bindQuotationRepository(impl: QuotationRepositoryImpl): QuotationRepository
}
