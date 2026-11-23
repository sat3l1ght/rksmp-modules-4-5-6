package com.example.todolist.domain.usecase

import com.example.todolist.domain.model.TodoItem
import com.example.todolist.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow

class ObserveTodosUseCase(private val repo: TodoRepository) {
    operator fun invoke(): Flow<List<TodoItem>> = repo.observeTodos()
}

class SaveTodoUseCase(private val repo: TodoRepository) {
    suspend operator fun invoke(item: TodoItem) {
        if (item.id == 0) repo.addTodo(item) else repo.updateTodo(item)
    }
}

class DeleteTodoUseCase(private val repo: TodoRepository) {
    suspend operator fun invoke(item: TodoItem) = repo.deleteTodo(item)
}
