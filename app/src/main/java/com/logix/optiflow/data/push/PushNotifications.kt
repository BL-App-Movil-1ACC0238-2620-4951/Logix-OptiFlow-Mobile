package com.logix.optiflow.data.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.logix.optiflow.MainActivity
import com.logix.optiflow.R

/** Canal y aviso de las notificaciones push del paciente. */
object PushNotifications {
    const val CHANNEL_ID = "optiflow_patient"

    /** Extra del intent que abre la pantalla de notificaciones; Firebase manda el mismo dato. */
    const val EXTRA_NOTIFICATION_ID = "notificationId"

    fun createChannel(context: Context) {
        val channel =
            NotificationChannel(CHANNEL_ID, "Citas y pedidos", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Recordatorios de citas, avisos de tus lentes y encuestas"
            }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Con la app abierta Firebase no muestra el aviso, así que se arma aquí. */
    fun show(context: Context, title: String, body: String, notificationId: String?) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        val intent =
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(EXTRA_NOTIFICATION_ID, notificationId.orEmpty())
        val pending =
            PendingIntent.getActivity(
                context,
                notificationId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.lucide_ic_bell)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pending)
                .build()
        try {
            manager.notify(notificationId.hashCode(), notification)
        } catch (_: SecurityException) {
            // El paciente quitó el permiso de notificaciones.
        }
    }
}
