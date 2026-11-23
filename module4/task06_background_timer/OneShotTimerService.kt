package com.example.module4.task06

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class OneShotTimerService : Service() {
    companion object {
        const val EXTRA_SECONDS = "extra_seconds"
        const val CHANNEL_ID = "timer_done_channel"
        const val NOTIFICATION_ID = 1002
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val seconds = intent?.getIntExtra(EXTRA_SECONDS, 10)?.coerceAtLeast(1) ?: 10
        scope.launch {
            delay(seconds * 1000L)
            showFinishedNotification()
            stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    private fun showFinishedNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Таймер", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Таймер завершён!")
            .setContentText("Время вышло")
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
