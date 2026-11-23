package com.example.module4.task04

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.random.Random

@Serializable
data class SocialPostDto(val id: Int, val userId: Int, val title: String, val body: String, val avatarUrl: String)

@Serializable
data class CommentDto(val postId: Int, val id: Int, val name: String, val body: String)

enum class CardLoadState { Loading, Ready, Error }

data class PostUiItem(
    val post: SocialPostDto,
    val avatarLoadedUrl: String? = null,
    val comments: List<CommentDto> = emptyList(),
    val state: CardLoadState = CardLoadState.Loading,
    val errorText: String? = null
)

private val json = Json { ignoreUnknownKeys = true }

@Composable
fun SocialFeedScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var feedJob by remember { mutableStateOf<Job?>(null) }
    val itemsMap = remember { mutableStateMapOf<Int, PostUiItem>() }

    fun loadFeed() {
        feedJob?.cancel()
        itemsMap.clear()
        feedJob = scope.launch {
            val (rawPosts, rawComments) = withContext(Dispatchers.IO) {
                val p = json.decodeFromString<List<SocialPostDto>>(
                    context.assets.open("social_posts.json").bufferedReader().use { it.readText() }
                )
                val c = json.decodeFromString<List<CommentDto>>(
                    context.assets.open("comments.json").bufferedReader().use { it.readText() }
                )
                p to c
            }

            rawPosts.forEach { itemsMap[it.id] = PostUiItem(post = it, state = CardLoadState.Loading) }

            supervisorScope {
                rawPosts.forEach { post ->
                    launch {
                        val avatarDef = async(Dispatchers.IO) {
                            runCatching {
                                delay(Random.nextLong(500L, 1800L))
                                if (Random.nextDouble() < 0.15) error("Ошибка загрузки аватарки")
                                post.avatarUrl
                            }
                        }
                        val commentsDef = async(Dispatchers.IO) {
                            runCatching {
                                delay(Random.nextLong(600L, 2200L))
                                if (Random.nextDouble() < 0.15) error("Ошибка загрузки комментариев")
                                rawComments.filter { it.postId == post.id }
                            }
                        }

                        val avatarRes = avatarDef.await()
                        val commentsRes = commentsDef.await()
                        val hasError = avatarRes.isFailure || commentsRes.isFailure

                        itemsMap[post.id] = PostUiItem(
                            post = post,
                            avatarLoadedUrl = avatarRes.getOrNull(),
                            comments = commentsRes.getOrDefault(emptyList()),
                            state = if (hasError) CardLoadState.Error else CardLoadState.Ready,
                            errorText = listOfNotNull(
                                avatarRes.exceptionOrNull()?.message,
                                commentsRes.exceptionOrNull()?.message
                            ).joinToString("; ").ifBlank { null }
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) { loadFeed() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Социальная лента", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { loadFeed() }) { Text("Обновить") }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(itemsMap.values.sortedBy { it.post.id }, key = { it.post.id }) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (item.avatarLoadedUrl != null) {
                                AsyncImage(
                                    model = item.avatarLoadedUrl,
                                    contentDescription = "Аватар",
                                    modifier = Modifier.size(40.dp)
                                )
                            } else {
                                Text(if (item.state == CardLoadState.Error) "[!]" else "[...]")
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(item.post.title, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(item.post.body)
                        Spacer(Modifier.height(8.dp))
                        when (item.state) {
                            CardLoadState.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                            CardLoadState.Error -> Text(
                                "Частичная ошибка: ${item.errorText}",
                                color = MaterialTheme.colorScheme.error
                            )
                            CardLoadState.Ready -> {
                                Text("Комментарии (${item.comments.size}):", style = MaterialTheme.typography.labelLarge)
                                item.comments.forEach { c ->
                                    Text("• ${c.name}: ${c.body}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
