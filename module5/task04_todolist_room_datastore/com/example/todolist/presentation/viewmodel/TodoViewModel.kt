package com.example.todolist.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.preferences.TodoPreferencesRepository
import com.example.todolist.domain.model.TodoItem
import com.example.todolist.domain.repository.TodoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TodoUiState(
    val items: List<TodoItem> = emptyList(),
    val highlightCompleted: Boolean = false
)

class TodoViewModel(
    private val repository: TodoRepository,
    private val prefs: TodoPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<TodoUiState> = combine(
        repository.observeTodos(),
        prefs.highlightCompleted
    ) { items, highlight ->
        TodoUiState(items = items, highlightCompleted = highlight)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodoUiState())

    init {
        viewModelScope.launch { repository.importInitialIfEmpty() }
    }

    fun toggleHighlightCompleted(enabled: Boolean) {
        viewModelScope.launch { prefs.setHighlightCompleted(enabled) }
    }

    fun save(item: TodoItem) {
        viewModelScope.launch {
            if (item.id == 0) repository.addTodo(item) else repository.updateTodo(item)
        }
    }

    fun delete(item: TodoItem) {
        viewModelScope.launch { repository.deleteTodo(item) }
    }
}
