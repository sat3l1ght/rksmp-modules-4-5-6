package com.example.module4.task07

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class RandomBoundService : Service() {
    inner class LocalBinder : Binder() {
        fun getService(): RandomBoundService = this@RandomBoundService
    }

    private val binder = LocalBinder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _randomNumber = MutableStateFlow(0)
    val randomNumber = _randomNumber.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        scope.launch {
            while (isActive) {
                _randomNumber.value = Random.nextInt(0, 101)
                delay(1000L)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
