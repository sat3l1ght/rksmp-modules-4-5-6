package com.example.module4.task09

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import kotlinx.coroutines.delay
import kotlin.random.Random

const val KEY_CITY = "city"
const val WEATHER_CHANNEL_ID = "weather_report_channel"
const val WEATHER_NOTIF_ID = 2001

private fun buildWeatherForegroundInfo(context: Context, text: String, ongoing: Boolean = true): ForegroundInfo {
    val nm = context.getSystemService(NotificationManager::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        nm.createNotificationChannel(
            NotificationChannel(WEATHER_CHANNEL_ID, "Прогноз погоды", NotificationManager.IMPORTANCE_LOW)
        )
    }
    val notif = NotificationCompat.Builder(context, WEATHER_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_menu_compass)
        .setContentTitle("Сбор прогноза погоды")
        .setContentText(text)
        .setOngoing(ongoing)
        .build()
    return ForegroundInfo(WEATHER_NOTIF_ID, notif)
}

class FetchCityWeatherWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val city = inputData.getString(KEY_CITY) ?: "Москва"
        setForeground(buildWeatherForegroundInfo(applicationContext, "Загружаем погоду: $city…"))
        delay(Random.nextLong(1200L, 2800L))
        val temp = Random.nextInt(-5, 26)
        return Result.success(workDataOf("temp_$city" to temp))
    }
}

class AggregateWeatherReportWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        setForeground(buildWeatherForegroundInfo(applicationContext, "Все данные получены, формируем отчёт…"))
        delay(1000L)
        val temps = inputData.keyValueMap.entries
            .filter { it.key.startsWith("temp_") }
            .mapNotNull { it.value as? Int }
        val avg = if (temps.isNotEmpty()) temps.average().toInt() else 0
        val sign = if (avg > 0) "+" else ""
        val summary = "Отчёт готов! Средняя температура $sign${avg}°C"
        val nm = applicationContext.getSystemService(NotificationManager::class.java)
        nm.notify(WEATHER_NOTIF_ID, buildWeatherForegroundInfo(applicationContext, summary, ongoing = false).notification)
        return Result.success(workDataOf("report" to summary))
    }
}

fun enqueueParallelWeatherReport(context: Context, cities: List<String> = listOf("Москва", "Лондон", "Нью-Йорк")) {
    val cityRequests = cities.map { city ->
        OneTimeWorkRequestBuilder<FetchCityWeatherWorker>()
            .setInputData(workDataOf(KEY_CITY to city))
            .build()
    }
    val aggregate = OneTimeWorkRequestBuilder<AggregateWeatherReportWorker>()
        .setInputMerger(OverwritingInputMerger::class.java)
        .build()

    WorkManager.getInstance(context)
        .beginUniqueWork("weather_parallel_report", ExistingWorkPolicy.REPLACE, cityRequests)
        .then(aggregate)
        .enqueue()
}
