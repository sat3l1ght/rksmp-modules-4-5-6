package com.example.filmdirectory.server

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.event.Level

object FilmsTable : IntIdTable("films") {
    val title = varchar("title", 255)
    val year = integer("year")
    val genre = varchar("genre", 100)
    val director = varchar("director", 150)
    val description = text("description")
    val rating = double("rating")
    val posterUrl = varchar("poster_url", 512)
}

@Serializable
data class FilmDto(
    val id: Int,
    val title: String,
    val year: Int,
    val genre: String,
    val director: String,
    val description: String,
    val rating: Double,
    val posterUrl: String
)

fun Application.filmDirectoryModule() {
    install(CallLogging) { level = Level.INFO }
    install(ContentNegotiation) { json() }
    install(CORS) { anyHost() }

    routing {
        get("/") { call.respond(mapOf("status" to "ok", "service" to "Справочник фильмов API")) }

        get("/films") {
            val query = call.request.queryParameters["query"].orEmpty().trim()
            val films = transaction {
                val rows = if (query.isBlank()) {
                    FilmsTable.selectAll()
                } else {
                    val pattern = "%${query.lowercase()}%"
                    FilmsTable.selectAll().where {
                        (FilmsTable.title.lowerCase() like pattern) or (FilmsTable.genre.lowerCase() like pattern)
                    }
                }
                rows.map {
                    FilmDto(
                        id = it[FilmsTable.id].value,
                        title = it[FilmsTable.title],
                        year = it[FilmsTable.year],
                        genre = it[FilmsTable.genre],
                        director = it[FilmsTable.director],
                        description = it[FilmsTable.description],
                        rating = it[FilmsTable.rating],
                        posterUrl = it[FilmsTable.posterUrl]
                    )
                }
            }
            call.respond(films)
        }

        get("/films/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            val film = transaction {
                FilmsTable.selectAll().where { FilmsTable.id eq id }.singleOrNull()?.let {
                    FilmDto(
                        id = it[FilmsTable.id].value,
                        title = it[FilmsTable.title],
                        year = it[FilmsTable.year],
                        genre = it[FilmsTable.genre],
                        director = it[FilmsTable.director],
                        description = it[FilmsTable.description],
                        rating = it[FilmsTable.rating],
                        posterUrl = it[FilmsTable.posterUrl]
                    )
                }
            }
            if (film != null) call.respond(film) else call.respond(HttpStatusCode.NotFound)
        }
    }
}
