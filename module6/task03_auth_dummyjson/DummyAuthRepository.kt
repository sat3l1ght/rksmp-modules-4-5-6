package com.example.module6.task03

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LoginPayload(val username: String, val password: String)

@Serializable
data class LoginResponse(val id: Int, val username: String, val accessToken: String = "", val token: String = "")

@Serializable
data class DummyUser(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val image: String
)

@Serializable
data class UsersListResponse(val users: List<DummyUser>)

class TokenStore(private val dataStore: DataStore<Preferences>) {
    private val TOKEN_KEY = stringPreferencesKey("bearer_token")

    suspend fun saveToken(token: String) {
        dataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun getToken(): String? = dataStore.data.map { it[TOKEN_KEY] }.first()

    suspend fun clearToken() {
        dataStore.edit { it.remove(TOKEN_KEY) }
    }
}

class DummyAuthRepository(private val tokenStore: TokenStore) {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    suspend fun login(username: String, password: String): LoginResponse {
        val resp: LoginResponse = client.post("https://dummyjson.com/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginPayload(username, password))
        }.body()
        val jwt = resp.accessToken.ifBlank { resp.token }
        tokenStore.saveToken(jwt)
        return resp
    }

    suspend fun getUsers(): List<DummyUser> {
        val token = tokenStore.getToken() ?: error("Пользователь не авторизован")
        val resp: UsersListResponse = client.get("https://dummyjson.com/users") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
        return resp.users
    }

    suspend fun getUserById(id: Int): DummyUser {
        val token = tokenStore.getToken() ?: error("Пользователь не авторизован")
        return client.get("https://dummyjson.com/users/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun logout() = tokenStore.clearToken()
}
