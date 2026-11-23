package com.example.module4.task08

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.delay

const val KEY_FILE_PATH = "file_path"
const val KEY_STEP_NAME = "step_name"
const val KEY_PROGRESS = "progress"

class CompressPhotoWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val inputPath = inputData.getString(KEY_FILE_PATH) ?: "photo_original.jpg"
        for (p in 0..100 step 25) {
            setProgress(workDataOf(KEY_STEP_NAME to "Сжимаем фото…", KEY_PROGRESS to p))
            delay(350L)
        }
        return Result.success(workDataOf(KEY_FILE_PATH to inputPath.replace(".jpg", "_compressed.jpg")))
    }
}

class WatermarkPhotoWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val path = inputData.getString(KEY_FILE_PATH) ?: return Result.failure()
        for (p in 0..100 step 25) {
            setProgress(workDataOf(KEY_STEP_NAME to "Добавляем водяной знак…", KEY_PROGRESS to p))
            delay(350L)
        }
        return Result.success(workDataOf(KEY_FILE_PATH to path.replace(".jpg", "_wm.jpg")))
    }
}

class UploadPhotoWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val path = inputData.getString(KEY_FILE_PATH) ?: return Result.failure()
        for (p in 0..100 step 20) {
            setProgress(workDataOf(KEY_STEP_NAME to "Загружаем в облако…", KEY_PROGRESS to p))
            delay(300L)
        }
        return Result.success(workDataOf(KEY_FILE_PATH to "https://cloud.example.com/uploads/$path"))
    }
}

fun enqueuePhotoProcessingChain(context: Context, photoFileName: String = "vacation.jpg") {
    val compress = OneTimeWorkRequestBuilder<CompressPhotoWorker>()
        .setInputData(workDataOf(KEY_FILE_PATH to photoFileName))
        .build()
    val watermark = OneTimeWorkRequestBuilder<WatermarkPhotoWorker>().build()
    val upload = OneTimeWorkRequestBuilder<UploadPhotoWorker>().build()

    WorkManager.getInstance(context)
        .beginUniqueWork("photo_upload_chain", ExistingWorkPolicy.REPLACE, compress)
        .then(watermark)
        .then(upload)
        .enqueue()
}
