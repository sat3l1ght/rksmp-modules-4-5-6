package com.example.module4.task01

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.random.Random
import kotlin.system.measureTimeMillis

@Serializable
data class UserDto(val id: Int, val name: String)

@Serializable
data class SaleItemDto(val product: String, val qty: Int, val revenue: Int)

@Serializable
data class SalesDto(val today: String, val items: List<SaleItemDto>)

@Serializable
data class WeatherDto(val city: String, val temp: Int, val condition: String)

private val json = Json { ignoreUnknownKeys = true }

suspend fun loadUsers(baseDir: File, failChance: Double = 0.2): List<String> = withContext(Dispatchers.IO) {
    delay(1800L)
    if (Random.nextDouble() < failChance) error("Сбой сети при загрузке списка пользователей")
    val content = File(baseDir, "users.json").readText()
    json.decodeFromString<List<UserDto>>(content).map { it.name }
}

suspend fun loadSales(baseDir: File, failChance: Double = 0.2): Map<String, Int> = withContext(Dispatchers.IO) {
    delay(1200L)
    if (Random.nextDouble() < failChance) error("Таймаут сервера статистики продаж")
    val content = File(baseDir, "sales.json").readText()
    json.decodeFromString<SalesDto>(content).items.associate { it.product to it.revenue }
}

suspend fun loadWeather(baseDir: File, failChance: Double = 0.2): List<String> = withContext(Dispatchers.IO) {
    delay(2500L)
    if (Random.nextDouble() < failChance) error("Сервис погоды временно недоступен")
    val content = File(baseDir, "weather.json").readText()
    json.decodeFromString<List<WeatherDto>>(content).map { "${it.city}: ${it.temp}°C" }
}

fun main() = runBlocking {
    val dataDir = File("data")
    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        println("[CoroutineExceptionHandler] Перехвачена ошибка: ${throwable.message}")
    }

    val elapsedMs = measureTimeMillis {
        supervisorScope {
            val usersDeferred = async(exceptionHandler) {
                runCatching { loadUsers(dataDir) }
            }
            val salesDeferred = async(exceptionHandler) {
                runCatching { loadSales(dataDir) }
            }
            val weatherDeferred = async(exceptionHandler) {
                runCatching { loadWeather(dataDir) }
            }

            val usersResult = usersDeferred.await()
            val salesResult = salesDeferred.await()
            val weatherResult = weatherDeferred.await()

            println("=== Итоги параллельной загрузки ===")
            usersResult
                .onSuccess { println("Пользователи: $it") }
                .onFailure { println("Ошибка загрузки пользователей: ${it.message}") }

            salesResult
                .onSuccess { println("Продажи (выручка): $it") }
                .onFailure { println("Ошибка загрузки продаж: ${it.message}") }

            weatherResult
                .onSuccess { println("Погода: $it") }
                .onFailure { println("Ошибка загрузки погоды: ${it.message}") }
        }
    }
    println("Общее время выполнения: ${elapsedMs} мс (ожидается ~2500 мс)")
}
