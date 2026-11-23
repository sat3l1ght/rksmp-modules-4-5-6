package com.example.todolist.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.todolist.domain.model.TodoItem
import com.example.todolist.presentation.viewmodel.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(viewModel: TodoViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Список задач") },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Цвет завершенных", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.width(6.dp))
                        Switch(
                            checked = state.highlightCompleted,
                            onCheckedChange = { viewModel.toggleHighlightCompleted(it) }
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.items, key = { it.id }) { item ->
                val bgColor = if (item.isCompleted && state.highlightCompleted) {
                    Color(0xFFDFF5E1)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = bgColor)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.isCompleted,
                            onCheckedChange = { viewModel.save(item.copy(isCompleted = it)) }
                        )
                        Column(Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            Text(item.description, style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = { viewModel.delete(item) }) { Text("Удалить") }
                    }
                }
            }
        }
    }
}
