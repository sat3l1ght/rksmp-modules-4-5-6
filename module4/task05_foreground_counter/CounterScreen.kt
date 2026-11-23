package com.example.module4.task05

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

@Composable
fun CounterScreen() {
    val context = LocalContext.current
    val seconds by CounterForegroundService.seconds.collectAsState()

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("$seconds с", fontSize = 56.sp)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = {
                val intent = Intent(context, CounterForegroundService::class.java)
                ContextCompat.startForegroundService(context, intent)
            }) { Text("Старт") }
            OutlinedButton(onClick = {
                context.stopService(Intent(context, CounterForegroundService::class.java))
            }) { Text("Стоп") }
        }
    }
}
