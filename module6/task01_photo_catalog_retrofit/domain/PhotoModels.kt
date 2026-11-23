package com.example.module6.task01.domain

data class Photo(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val url: String,
    val downloadUrl: String
)

interface PhotoRepository {
    suspend fun getPhotos(page: Int = 1, limit: Int = 30): List<Photo>
}

class GetPhotosUseCase(private val repo: PhotoRepository) {
    suspend operator fun invoke(page: Int = 1, limit: Int = 30): List<Photo> = repo.getPhotos(page, limit)
}
