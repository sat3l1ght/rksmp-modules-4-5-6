package com.example.module5.task01

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiaryListScreen(
    viewModel: DiaryViewModel,
    onCreateNew: () -> Unit,
    onOpenEntry: (DiaryEntry) -> Unit
) {
    val entries by viewModel.entries.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onCreateNew) {
                Text("+ Новая запись")
            }
        }
    ) { padding ->
        if (entries.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("У вас пока нет записей", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Нажмите +, чтобы создать первую")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(entries, key = { it.fileName }) { entry ->
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onOpenEntry(entry) },
                                    onLongClick = { menuExpanded = true }
                                )
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(entry.title, style = MaterialTheme.typography.titleMedium)
                                Text(entry.dateFormatted, style = MaterialTheme.typography.labelSmall)
                                Spacer(Modifier.height(4.dp))
                                Text(entry.preview, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Удалить") },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.deleteEntry(entry.fileName)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
