package com.example.todolist.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.todoDataStore: DataStore<Preferences> by preferencesDataStore(name = "todo_settings")

class TodoPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val HIGHLIGHT_COMPLETED = booleanPreferencesKey("highlight_completed_color")
        private val INITIAL_IMPORT_DONE = booleanPreferencesKey("initial_json_imported")
    }

    val highlightCompleted: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[HIGHLIGHT_COMPLETED] ?: false }

    val isInitialImportDone: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[INITIAL_IMPORT_DONE] ?: false }

    suspend fun setHighlightCompleted(enabled: Boolean) {
        dataStore.edit { it[HIGHLIGHT_COMPLETED] = enabled }
    }

    suspend fun markInitialImportDone() {
        dataStore.edit { it[INITIAL_IMPORT_DONE] = true }
    }
}
