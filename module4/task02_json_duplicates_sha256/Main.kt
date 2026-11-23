package com.example.module4.task02

import kotlinx.coroutines.*
import java.io.File
import java.security.MessageDigest

suspend fun computeSha256(file: File): String = withContext(Dispatchers.IO) {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().use { input ->
        val buffer = ByteArray(8192)
        var read = input.read(buffer)
        while (read != -1) {
            ensureActive()
            digest.update(buffer, 0, read)
            read = input.read(buffer)
        }
    }
    digest.digest().joinToString("") { "%02x".format(it) }
}

suspend fun findJsonDuplicates(rootDir: File, timeoutMs: Long): Map<String, List<File>>? {
    return withTimeoutOrNull(timeoutMs) {
        val jsonFiles = withContext(Dispatchers.IO) {
            rootDir.walkTopDown()
                .filter { it.isFile && it.extension.equals("json", ignoreCase = true) }
                .toList()
        }
        val hashed = coroutineScope {
            jsonFiles.map { file ->
                async(Dispatchers.IO) {
                    file to computeSha256(file)
                }
            }.awaitAll()
        }
        hashed.groupBy({ it.second }, { it.first })
            .filterValues { it.size > 1 }
    }
}

fun main(args: Array<String>) = runBlocking {
    val targetDir = File(args.getOrElse(0) { "./sample_json_dir" })
    val timeoutSec = args.getOrElse(1) { "5" }.toLongOrNull() ?: 5L

    if (!targetDir.exists() || !targetDir.isDirectory) {
        println("Директория не найдена: ${targetDir.absolutePath}")
        return@runBlocking
    }

    val duplicates = findJsonDuplicates(targetDir, timeoutSec * 1000L)
    if (duplicates == null) {
        println("Поиск прерван по таймауту (${timeoutSec} с)")
    } else if (duplicates.isEmpty()) {
        println("Дубликаты JSON-файлов не найдены.")
    } else {
        println("Найдено групп дубликатов: ${duplicates.size}")
        duplicates.forEach { (hash, files) ->
            println("SHA-256: $hash")
            files.forEach { println("  - ${it.path}") }
        }
    }
}
