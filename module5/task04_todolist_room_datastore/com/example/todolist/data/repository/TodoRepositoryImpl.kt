package com.example.todolist.data.repository

import android.content.Context
import com.example.todolist.data.local.TodoDao
import com.example.todolist.data.local.TodoEntity
import com.example.todolist.data.preferences.TodoPreferencesRepository
import com.example.todolist.domain.model.TodoItem
import com.example.todolist.domain.repository.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class TodoSeedDto(val title: String, val description: String, val isCompleted: Boolean)

class TodoRepositoryImpl(
    private val context: Context,
    private val dao: TodoDao,
    private val prefs: TodoPreferencesRepository
) : TodoRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override fun observeTodos(): Flow<List<TodoItem>> =
        dao.observeAll().map { list ->
            list.map { TodoItem(it.id, it.title, it.description, it.isCompleted) }
        }

    override suspend fun addTodo(item: TodoItem) =
        dao.insert(TodoEntity(title = item.title, description = item.description, isCompleted = item.isCompleted))

    override suspend fun updateTodo(item: TodoItem) =
        dao.update(TodoEntity(item.id, item.title, item.description, item.isCompleted))

    override suspend fun deleteTodo(item: TodoItem) =
        dao.delete(TodoEntity(item.id, item.title, item.description, item.isCompleted))

    override suspend fun importInitialIfEmpty() {
        val alreadyImported = prefs.isInitialImportDone.first()
        if (alreadyImported || dao.count() > 0) return
        withContext(Dispatchers.IO) {
            val raw = context.assets.open("initial_todos.json").bufferedReader().use { it.readText() }
            val seeds = json.decodeFromString<List<TodoSeedDto>>(raw)
            dao.insertAll(seeds.map { TodoEntity(title = it.title, description = it.description, isCompleted = it.isCompleted) })
            prefs.markInitialImportDone()
        }
    }
}
