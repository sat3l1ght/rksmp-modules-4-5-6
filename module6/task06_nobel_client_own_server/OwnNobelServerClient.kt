package com.example.module6.task06

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ServerPrizeDto(val id: Int, val awardYear: Int, val category: String, val fullName: String, val motivation: String = "")

class OwnNobelServerClient(private val baseUrl: String = "http://10.0.2.2:8080") {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    suspend fun getPrizes(): List<ServerPrizeDto> = client.get("$baseUrl/prizes").body()

    suspend fun getFavoritePrizes(token: String): List<ServerPrizeDto> =
        client.get("$baseUrl/users/me/prizes") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()

    suspend fun addFavorite(token: String, prizeId: Int) {
        client.post("$baseUrl/users/me/prizes/$prizeId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    suspend fun removeFavorite(token: String, prizeId: Int) {
        client.delete("$baseUrl/users/me/prizes/$prizeId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}
