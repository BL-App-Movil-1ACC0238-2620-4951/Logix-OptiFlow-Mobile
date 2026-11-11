package com.logix.optiflow

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.logix.optiflow.data.push.PushNotifications
import com.logix.optiflow.presentation.navigation.OptiFlowNavHost
import com.logix.optiflow.presentation.theme.OptiFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /** true cuando la app se abrió tocando un push: se va a la pantalla de notificaciones. */
    private val openNotifications = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) openNotifications.value = intent.isPush()
        enableEdgeToEdge()
        setContent {
            OptiFlowTheme {
                OptiFlowNavHost(
                    openNotifications = openNotifications.value,
                    onNotificationsOpened = { openNotifications.value = false },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.isPush()) openNotifications.value = true
    }

    private fun Intent.isPush() = hasExtra(PushNotifications.EXTRA_NOTIFICATION_ID)
}
