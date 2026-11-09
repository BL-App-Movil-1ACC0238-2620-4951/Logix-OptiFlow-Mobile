package com.logix.optiflow.di

import com.logix.optiflow.BuildConfig
import com.logix.optiflow.data.remote.AuthInterceptor
import com.logix.optiflow.data.remote.AuthTokenHolder
import com.logix.optiflow.data.remote.json.InstantJsonAdapter
import com.logix.optiflow.data.remote.json.UuidJsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {

    private const val TIMEOUT_SECONDS = 60L

    fun moshi(): Moshi =
        Moshi.Builder()
            .add(UuidJsonAdapter())
            .add(InstantJsonAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()

    fun okHttpClient(): OkHttpClient {
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
            .addInterceptor(logging)
            .build()
    }

    fun retrofit(
        baseUrl: String = BuildConfig.API_BASE_URL,
        client: OkHttpClient = okHttpClient(),
        moshi: Moshi = moshi(),
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
}
