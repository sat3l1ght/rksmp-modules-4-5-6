package com.example.nobel

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.slf4j.event.Level
import java.util.Date

@Serializable
data class LoginReq(val username: String, val password: String)

@Serializable
data class TokenResp(val token: String)

@Serializable
data class Laureate(val id: Int, val fullName: String, val portion: String, val motivation: String)

@Serializable
data class NobelPrize(val year: Int, val category: String, val laureates: List<Laureate>)

object JwtConfig {
    const val SECRET = "super-secret-nobel-jwt-signing-key-at-least-32-chars!"
    const val ISSUER = "nobel-ktor-server"
    const val AUDIENCE = "nobel-mobile-client"
    private const val VALIDITY_MS = 30 * 60 * 1000L // 30 минут

    val verifier = JWT.require(Algorithm.HMAC256(SECRET))
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()

    fun generateToken(username: String): String = JWT.create()
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .withClaim("username", username)
        .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_MS))
        .sign(Algorithm.HMAC256(SECRET))
}

class InMemoryNobelRepository {
    private val prizes = listOf(
        NobelPrize(2023, "physics", listOf(
            Laureate(1, "Pierre Agostini", "1/3", "for experimental methods that generate attosecond pulses of light"),
            Laureate(2, "Ferenc Krausz", "1/3", "for experimental methods that generate attosecond pulses of light")
        )),
        NobelPrize(2024, "physics", listOf(
            Laureate(3, "John J. Hopfield", "1/2", "for foundational discoveries enabling machine learning with artificial neural networks"),
            Laureate(4, "Geoffrey E. Hinton", "1/2", "for foundational discoveries enabling machine learning with artificial neural networks")
        )),
        NobelPrize(2024, "chemistry", listOf(
            Laureate(5, "David Baker", "1/2", "for computational protein design"),
            Laureate(6, "Demis Hassabis", "1/4", "for protein structure prediction")
        ))
    )

    fun getAll(): List<NobelPrize> = prizes
    fun findByYearAndCategory(year: Int, category: String): NobelPrize? =
        prizes.firstOrNull { it.year == year && it.category.equals(category, ignoreCase = true) }
}

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::nobelModule).start(wait = true)
}

fun Application.nobelModule() {
    val repository = InMemoryNobelRepository()

    install(CallLogging) { level = Level.INFO }
    install(ContentNegotiation) { json() }
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JwtConfig.verifier)
            validate { cred ->
                if (cred.payload.getClaim("username").asString().isNotBlank()) JWTPrincipal(cred.payload) else null
            }
            challenge { _, _ -> call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or expired token")) }
        }
    }

    routing {
        post("/auth/login") {
            val req = call.receive<LoginReq>()
            if (req.username == "admin" && req.password == "secret123") {
                call.respond(TokenResp(JwtConfig.generateToken(req.username)))
            } else {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Неверный логин или пароль"))
            }
        }

        authenticate("auth-jwt") {
            get("/prizes") {
                call.respond(repository.getAll())
            }
            get("/prizes/{year}/{category}") {
                val year = call.parameters["year"]?.toIntOrNull()
                val cat = call.parameters["category"]
                val prize = if (year != null && cat != null) repository.findByYearAndCategory(year, cat) else null
                if (prize != null) call.respond(prize) else call.respond(HttpStatusCode.NotFound)
            }
            get("/prizes/{year}/{category}/laureates") {
                val year = call.parameters["year"]?.toIntOrNull()
                val cat = call.parameters["category"]
                val prize = if (year != null && cat != null) repository.findByYearAndCategory(year, cat) else null
                if (prize != null) call.respond(prize.laureates) else call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
