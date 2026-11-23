package com.example.module4.task03

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
import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GitHubRepo(
    val id: Int,
    @SerialName("full_name") val fullName: String,
    val description: String? = null,
    @SerialName("stargazers_count") val stargazersCount: Int,
    val language: String? = null
)

fun <T> CoroutineScope.debounce(
    waitMs: Long = 500L,
    destinationFunction: suspend (T) -> Unit
): (T) -> Unit {
    var debounceJob: Job? = null
    return { param: T ->
        debounceJob?.cancel()
        debounceJob = launch {
            delay(waitMs)
            destinationFunction(param)
        }
    }
}

private val jsonParser = Json { ignoreUnknownKeys = true }

private suspend fun loadReposFromAssets(context: Context): List<GitHubRepo> =
    withContext(Dispatchers.IO) {
        val raw = context.assets.open("github_repos.json").bufferedReader().use { it.readText() }
        jsonParser.decodeFromString<List<GitHubRepo>>(raw)
    }

@Composable
fun GitHubSearchScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var allRepos by remember { mutableStateOf<List<GitHubRepo>>(emptyList()) }
    var filteredRepos by remember { mutableStateOf<List<GitHubRepo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) {
        allRepos = loadReposFromAssets(context)
        filteredRepos = allRepos
    }

    val debouncedSearch = remember(allRepos) {
        scope.debounce<String>(waitMs = 500L) { text ->
            searchJob?.cancel()
            searchJob = scope.launch {
                isLoading = true
                try {
                    val result = withContext(Dispatchers.Default) {
                        delay(300L) // имитация сетевого поиска
                        if (text.isBlank()) allRepos
                        else allRepos.filter {
                            it.fullName.contains(text, ignoreCase = true) ||
                                (it.description?.contains(text, ignoreCase = true) == true) ||
                                (it.language?.contains(text, ignoreCase = true) == true)
                        }
                    }
                    filteredRepos = result
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Поиск репозиториев GitHub", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { newText ->
                query = newText
                debouncedSearch(newText)
            },
            label = { Text("Название, описание или язык") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        if (isLoading) {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredRepos, key = { it.id }) { repo ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(repo.fullName, style = MaterialTheme.typography.titleMedium)
                        repo.description?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(it, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "★ ${repo.stargazersCount}  •  ${repo.language ?: "Unknown"}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}
