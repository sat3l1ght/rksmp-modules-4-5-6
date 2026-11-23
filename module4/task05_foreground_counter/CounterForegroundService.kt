package com.example.module4.task05

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CounterForegroundService : Service() {
    companion object {
        const val CHANNEL_ID = "counter_fg_channel"
        const val NOTIFICATION_ID = 1001
        private val _seconds = MutableStateFlow(0)
        val seconds = _seconds.asStateFlow()
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var counterJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification(_seconds.value))
        counterJob?.cancel()
        counterJob = serviceScope.launch {
            while (isActive) {
                delay(1000L)
                _seconds.value += 1
                val nm = getSystemService(NotificationManager::class.java)
                nm.notify(NOTIFICATION_ID, buildNotification(_seconds.value))
            }
        }
        return START_STICKY
    }

    private fun buildNotification(sec: Int): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Счётчик времени")
            .setContentText("Прошло $sec секунд")
            .setOngoing(true)
            .build()

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "Счётчик", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    override fun onDestroy() {
        counterJob?.cancel()
        serviceScope.cancel()
        _seconds.value = 0
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
