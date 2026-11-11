package com.logix.optiflow.data.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.logix.optiflow.domain.repository.notification.NotificationRepository
import com.logix.optiflow.domain.repository.searchbooking.PatientRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** Recibe los push del backend y avisa cuando Firebase cambia el token del celular. */
@AndroidEntryPoint
class OptiFlowMessagingService : FirebaseMessagingService() {

    @Inject lateinit var patientRepository: PatientRepository

    @Inject lateinit var notificationRepository: NotificationRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        scope.launch {
            val session = patientRepository.getSession()?.takeIf { it.isPatient && !it.token.isNullOrBlank() }
            session?.let { notificationRepository.registerDevice(it.patientId) }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val notification = message.notification ?: return
        PushNotifications.show(
            context = this,
            title = notification.title.orEmpty(),
            body = notification.body.orEmpty(),
            notificationId = message.data[PushNotifications.EXTRA_NOTIFICATION_ID],
        )
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
