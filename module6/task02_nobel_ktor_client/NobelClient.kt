package com.example.module6.task02

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LocalizedText(val en: String = "")

@Serializable
data class LaureateDto(
    val id: String = "",
    val fullName: LocalizedText? = null,
    val motivation: LocalizedText? = null
)

@Serializable
data class NobelPrizeDto(
    val awardYear: String = "",
    val category: LocalizedText = LocalizedText(),
    val laureates: List<LaureateDto> = emptyList()
)

@Serializable
data class NobelResponseDto(val nobelPrizes: List<NobelPrizeDto> = emptyList())

data class NobelPrizeItem(
    val year: String,
    val category: String,
    val laureateName: String,
    val motivation: String
)

interface NobelRepository {
    suspend fun getPrizes(year: Int? = null, category: String? = null): List<NobelPrizeItem>
}

class NobelRepositoryImpl : NobelRepository {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override suspend fun getPrizes(year: Int?, category: String?): List<NobelPrizeItem> {
        val response: NobelResponseDto = client.get("https://api.nobelprize.org/2.1/nobelPrizes") {
            parameter("limit", 25)
            parameter("offset", 0)
            year?.let { parameter("nobelPrizeYear", it) }
            category?.takeIf { it.isNotBlank() }?.let { parameter("nobelPrizeCategory", it) }
        }.body()

        return response.nobelPrizes.flatMap { prize ->
            prize.laureates.map { laureate ->
                NobelPrizeItem(
                    year = prize.awardYear,
                    category = prize.category.en,
                    laureateName = laureate.fullName?.en ?: "Не указано",
                    motivation = laureate.motivation?.en ?: ""
                )
            }
        }
    }
}
