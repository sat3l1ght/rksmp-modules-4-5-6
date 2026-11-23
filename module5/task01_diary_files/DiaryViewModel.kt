package com.example.module5.task01

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DiaryEntry(
    val fileName: String,
    val title: String,
    val preview: String,
    val fullText: String,
    val dateFormatted: String
)

class DiaryViewModel(application: Application) : AndroidViewModel(application) {
    private val filesDir: File = application.filesDir
    private val dateFmt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru", "RU"))

    private val _entries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val entries: StateFlow<List<DiaryEntry>> = _entries.asStateFlow()

    init {
        // Полное сканирование выполняется только один раз при старте
        viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) {
                filesDir.listFiles { f -> f.isFile && f.extension == "txt" }
                    ?.sortedByDescending { it.lastModified() }
                    ?.map { readEntryFromFile(it) }
                    ?: emptyList()
            }
            _entries.value = loaded
        }
    }

    fun saveEntry(title: String, body: String, existingFileName: String? = null) {
        viewModelScope.launch {
            val entry = withContext(Dispatchers.IO) {
                val safeTitle = title.trim().replace(Regex("[^a-zA-Zа-яА-Я0-9_-]"), "_")
                val fileName = existingFileName ?: if (safeTitle.isNotEmpty()) {
                    "${System.currentTimeMillis()}_${safeTitle}.txt"
                } else {
                    "${System.currentTimeMillis()}.txt"
                }
                val file = File(filesDir, fileName)
                file.writeText("${title.trim()}
$body")
                readEntryFromFile(file)
            }
            // Добавляем в начало списка или обновляем без повторного сканирования папки
            _entries.update { current ->
                listOf(entry) + current.filterNot { it.fileName == entry.fileName }
            }
        }
    }

    fun deleteEntry(fileName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                File(filesDir, fileName).delete()
            }
            // Удаляем из текущего списка без пересканирования папки
            _entries.update { list -> list.filterNot { it.fileName == fileName } }
        }
    }

    private fun readEntryFromFile(file: File): DiaryEntry {
        val lines = file.readLines()
        val title = lines.firstOrNull().orEmpty().ifBlank { "Без заголовка" }
        val body = lines.drop(1).joinToString("
")
        val preview = body.replace("
", " ").take(40)
        return DiaryEntry(
            fileName = file.name,
            title = title,
            preview = preview,
            fullText = body,
            dateFormatted = dateFmt.format(Date(file.lastModified()))
        )
    }
}
