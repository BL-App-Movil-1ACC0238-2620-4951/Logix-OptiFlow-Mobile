package com.logix.optiflow.di

import com.logix.optiflow.BuildConfig
import com.logix.optiflow.data.remote.AuthInterceptor
import com.logix.optiflow.data.remote.AuthTokenHolder
import com.logix.optiflow.data.remote.SessionEvents
import com.logix.optiflow.data.remote.UnauthorizedInterceptor
import com.logix.optiflow.data.remote.api.clinical.ClinicalApi
import com.logix.optiflow.data.remote.api.inventory.InventoryApi
import com.logix.optiflow.data.remote.api.report.ReportApi
import com.logix.optiflow.data.remote.api.notification.NotificationApi
import com.logix.optiflow.data.remote.api.production.ProductionApi
import com.logix.optiflow.data.remote.api.searchbooking.SearchBookingApi
import com.logix.optiflow.data.remote.json.InstantJsonAdapter
import com.logix.optiflow.data.remote.json.UuidJsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIMEOUT_SECONDS = 60L

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(UuidJsonAdapter())
            .add(InstantJsonAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(sessionEvents: SessionEvents): OkHttpClient {
        val logging =
            HttpLoggingInterceptor().apply {
                level =
                    if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
            }

        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor { AuthTokenHolder.token })
            .addInterceptor(UnauthorizedInterceptor(sessionEvents))
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideSearchBookingApi(retrofit: Retrofit): SearchBookingApi =
        retrofit.create(SearchBookingApi::class.java)

    @Provides
    @Singleton
    fun provideInventoryApi(retrofit: Retrofit): InventoryApi =
        retrofit.create(InventoryApi::class.java)

    @Provides
    @Singleton
    fun provideReportApi(retrofit: Retrofit): ReportApi =
        retrofit.create(ReportApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi =
        retrofit.create(NotificationApi::class.java)

    @Provides
    @Singleton
    fun provideProductionApi(retrofit: Retrofit): ProductionApi =
        retrofit.create(ProductionApi::class.java)

    @Provides
    @Singleton
    fun provideClinicalApi(retrofit: Retrofit): ClinicalApi =
        retrofit.create(ClinicalApi::class.java)
}
