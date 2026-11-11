package com.logix.optiflow

import android.app.Application
import com.logix.optiflow.data.push.PushNotifications
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OptiFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PushNotifications.createChannel(this)
    }
}
