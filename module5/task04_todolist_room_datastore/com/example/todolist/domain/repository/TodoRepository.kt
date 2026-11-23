package com.example.todolist.domain.repository

import com.example.todolist.domain.model.TodoItem
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun observeTodos(): Flow<List<TodoItem>>
    suspend fun addTodo(item: TodoItem)
    suspend fun updateTodo(item: TodoItem)
    suspend fun deleteTodo(item: TodoItem)
    suspend fun importInitialIfEmpty()
}
