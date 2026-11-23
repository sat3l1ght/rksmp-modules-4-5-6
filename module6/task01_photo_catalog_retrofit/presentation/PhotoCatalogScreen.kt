package com.example.module6.task01.presentation

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.module6.task01.domain.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

sealed interface PhotosUiState {
    object Loading : PhotosUiState
    data class Success(val photos: List<Photo>) : PhotosUiState
    data class Error(val message: String) : PhotosUiState
}

@Composable
fun PhotoDetailScreen(photo: Photo, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<String?>(null) }

    val safLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("image/jpeg")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val ok = saveUrlToUri(context, photo.downloadUrl, uri)
                status = if (ok) "Фото сохранено в Downloads" else "Ошибка сохранения"
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("← Назад") }
        AsyncImage(model = photo.downloadUrl, contentDescription = photo.author, modifier = Modifier.fillMaxWidth().height(300.dp))
        Spacer(Modifier.height(12.dp))
        Text("Автор: ${photo.author}", style = MaterialTheme.typography.titleMedium)
        Text("Размеры: ${photo.width} × ${photo.height}")
        Text("Ссылка: ${photo.url}", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))
        Button(onClick = { safLauncher.launch("picsum_${photo.id}.jpg") }) {
            Text("Скачать фото")
        }
        status?.let { Spacer(Modifier.height(8.dp)); Text(it) }
    }
}

private suspend fun saveUrlToUri(context: Context, imageUrl: String, targetUri: Uri): Boolean =
    withContext(Dispatchers.IO) {
        runCatching {
            URL(imageUrl).openStream().use { input ->
                context.contentResolver.openOutputStream(targetUri)?.use { output ->
                    input.copyTo(output)
                }
            }
            true
        }.getOrDefault(false)
    }
