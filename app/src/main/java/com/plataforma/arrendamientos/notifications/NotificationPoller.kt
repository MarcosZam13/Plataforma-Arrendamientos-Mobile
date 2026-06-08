package com.plataforma.arrendamientos.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.plataforma.arrendamientos.MainActivity
import com.plataforma.arrendamientos.R
import com.plataforma.arrendamientos.data.repository.DataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val CHANNEL_ID = "notificaciones_arrendamientos"
private const val POLL_INTERVAL_MS = 15_000L

@Singleton
class NotificationPoller @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataRepository: DataRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollingJob: Job? = null
    private val seenIds = mutableSetOf<String>()

    fun start(userId: String) {
        if (pollingJob?.isActive == true) return
        ensureChannel()
        pollingJob = scope.launch {
            // Carga inicial — marcar existentes como ya vistos (no molestar con historial)
            dataRepository.refreshNotifications(userId)
            seenIds += dataRepository.getNotificationsByUser(userId).map { it.id }

            while (isActive) {
                delay(POLL_INTERVAL_MS)
                dataRepository.refreshNotifications(userId).onSuccess {
                    val nuevas = dataRepository.getNotificationsByUser(userId)
                        .filter { it.id !in seenIds }
                    nuevas.forEach { notif ->
                        mostrar(notif.titulo, notif.mensaje)
                        seenIds.add(notif.id)
                    }
                }
            }
        }
    }

    fun stop() {
        pollingJob?.cancel()
        pollingJob = null
        seenIds.clear()
    }

    private fun mostrar(titulo: String, cuerpo: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun ensureChannel() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notificaciones Arrendamientos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Mensajes y alertas de tu cuenta"
            }
            manager.createNotificationChannel(channel)
        }
    }
}
