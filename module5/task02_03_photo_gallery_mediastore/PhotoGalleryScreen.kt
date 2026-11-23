package com.example.module5.task02_03

import android.Manifest
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PhotoGalleryScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var photos by remember { mutableStateOf<List<File>>(emptyList()) }
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }
    var selectedPhoto by remember { mutableStateOf<File?>(null) }

    fun reloadPhotos() {
        photos = PhotoStorageHelper.listPrivatePhotos(context)
    }

    LaunchedEffect(Unit) { reloadPhotos() }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) reloadPhotos() else pendingPhotoFile?.delete()
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = PhotoStorageHelper.createPrivatePhotoFile(context)
            pendingPhotoFile = file
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            takePictureLauncher.launch(uri)
        }
    }

    fun exportPhoto(file: File) {
        scope.launch {
            val ok = withContext(Dispatchers.IO) { PhotoStorageHelper.exportToGallery(context, file) }
            snackbarHostState.showSnackbar(
                if (ok) "Фото добавлено в галерею" else "Ошибка экспорта фото"
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (photos.isNotEmpty()) {
                ExtendedFloatingActionButton(onClick = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }) { Text("Сделать фото") }
            }
        }
    ) { padding ->
        if (photos.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("У вас пока нет фото", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }) { Text("Сделать первое фото") }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().padding(padding).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(photos, key = { it.absolutePath }) { photo ->
                    var menuOpen by remember { mutableStateOf(false) }
                    Box(Modifier.aspectRatio(1f)) {
                        AsyncImage(
                            model = photo,
                            contentDescription = photo.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clickable { selectedPhoto = photo }
                        )
                        TextButton(
                            onClick = { menuOpen = true },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) { Text("⋮") }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Экспорт в галерею") },
                                onClick = {
                                    menuOpen = false
                                    exportPhoto(photo)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedPhoto?.let { photo ->
        AlertDialog(
            onDismissRequest = { selectedPhoto = null },
            title = { Text(photo.name) },
            text = {
                AsyncImage(model = photo, contentDescription = photo.name, modifier = Modifier.fillMaxWidth().height(320.dp))
            },
            confirmButton = {
                Button(onClick = {
                    exportPhoto(photo)
                    selectedPhoto = null
                }) { Text("Экспорт в галерею") }
            },
            dismissButton = {
                TextButton(onClick = { selectedPhoto = null }) { Text("Закрыть") }
            }
        )
    }
}
