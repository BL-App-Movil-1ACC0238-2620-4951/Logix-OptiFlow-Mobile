package com.logix.optiflow.data.remote

import com.logix.optiflow.data.remote.api.searchbooking.SearchBookingApi
import com.logix.optiflow.di.NetworkModule
import retrofit2.Retrofit

object ApiClient {
    val retrofit: Retrofit by lazy { NetworkModule.retrofit() }

    val searchBookingApi: SearchBookingApi by lazy {
        retrofit.create(SearchBookingApi::class.java)
    }
}
