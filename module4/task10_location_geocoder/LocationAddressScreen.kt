package com.example.module4.task10

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

@SuppressLint("MissingPermission")
private suspend fun fetchCurrentLocation(context: Context): Pair<Double, Double>? =
    suspendCancellableCoroutine { cont ->
        val client = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                cont.resume(loc?.let { it.latitude to it.longitude })
            }
            .addOnFailureListener { cont.resume(null) }
        cont.invokeOnCancellation { cts.cancel() }
    }

private suspend fun reverseGeocode(context: Context, lat: Double, lng: Double): String =
    withContext(Dispatchers.IO) {
        val geocoder = Geocoder(context, Locale("ru", "RU"))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(lat, lng, 1) { list ->
                    val addr = list.firstOrNull()?.getAddressLine(0) ?: "Адрес не определён"
                    cont.resume(addr)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()?.getAddressLine(0) ?: "Адрес не определён"
        }
    }

@Composable
fun LocationAddressScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf<String?>(null) }
    var coords by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun loadLocation() {
        scope.launch {
            isLoading = true
            error = null
            try {
                val loc = fetchCurrentLocation(context)
                if (loc == null) {
                    error = "Не удалось получить координаты (проверьте GPS)"
                } else {
                    coords = String.format(Locale.US, "lat: %.5f / lng: %.5f", loc.first, loc.second)
                    address = reverseGeocode(context, loc.first, loc.second)
                }
            } catch (e: Exception) {
                error = "Ошибка геокодирования: ${e.localizedMessage ?: "нет сети"}"
            } finally {
                isLoading = false
            }
        }
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            granted[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            loadLocation()
        } else {
            error = "Пользователь отказал в разрешении на доступ к геолокации"
        }
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            permLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }) { Text("Получить мой адрес") }

        Spacer(Modifier.height(20.dp))
        if (isLoading) CircularProgressIndicator()
        address?.let {
            Text(it, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
        }
        coords?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
