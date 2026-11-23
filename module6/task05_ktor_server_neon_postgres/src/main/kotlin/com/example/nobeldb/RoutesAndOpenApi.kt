package com.example.nobeldb

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureNobelDbRoutes() {
    routing {
        get("/prizes") {
            val list = transaction {
                PrizesTable.selectAll().map {
                    mapOf(
                        "id" to it[PrizesTable.id].value,
                        "awardYear" to it[PrizesTable.awardYear],
                        "category" to it[PrizesTable.category],
                        "fullName" to it[PrizesTable.fullName],
                        "motivation" to it[PrizesTable.motivation]
                    )
                }
            }
            call.respond(list)
        }

        authenticate("auth-jwt") {
            get("/users/me") {
                val username = call.principal<JWTPrincipal>()!!.payload.getClaim("username").asString()
                call.respond(mapOf("username" to username))
            }

            get("/users/me/prizes") {
                val username = call.principal<JWTPrincipal>()!!.payload.getClaim("username").asString()
                val favs = transaction {
                    val userId = UsersTable.selectAll().where { UsersTable.username eq username }
                        .single()[UsersTable.id]
                    (UserPrizesTable innerJoin PrizesTable)
                        .selectAll().where { UserPrizesTable.userId eq userId }
                        .map {
                            mapOf(
                                "id" to it[PrizesTable.id].value,
                                "awardYear" to it[PrizesTable.awardYear],
                                "category" to it[PrizesTable.category],
                                "fullName" to it[PrizesTable.fullName]
                            )
                        }
                }
                call.respond(favs)
            }

            post("/users/me/prizes/{prizeId}") {
                val username = call.principal<JWTPrincipal>()!!.payload.getClaim("username").asString()
                val prizeId = call.parameters["prizeId"]?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest)
                transaction {
                    val userId = UsersTable.selectAll().where { UsersTable.username eq username }
                        .single()[UsersTable.id]
                    UserPrizesTable.insertIgnore {
                        it[UserPrizesTable.userId] = userId
                        it[UserPrizesTable.prizeId] = prizeId
                        it[addedAt] = System.currentTimeMillis()
                    }
                }
                call.respond(HttpStatusCode.Created)
            }

            delete("/users/me/prizes/{prizeId}") {
                val username = call.principal<JWTPrincipal>()!!.payload.getClaim("username").asString()
                val prizeId = call.parameters["prizeId"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)
                transaction {
                    val userId = UsersTable.selectAll().where { UsersTable.username eq username }
                        .single()[UsersTable.id]
                    UserPrizesTable.deleteWhere {
                        (UserPrizesTable.userId eq userId) and (UserPrizesTable.prizeId eq prizeId)
                    }
                }
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
