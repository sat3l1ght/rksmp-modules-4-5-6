package com.example.module6.task01.data

import com.example.module6.task01.domain.Photo
import com.example.module6.task01.domain.PhotoRepository
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class PhotoDto(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val url: String,
    @SerialName("download_url") val downloadUrl: String
)

interface PicsumApi {
    @GET("v2/list")
    suspend fun getPhotos(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 30
    ): List<PhotoDto>
}

class PhotoRepositoryImpl(private val api: PicsumApi) : PhotoRepository {
    override suspend fun getPhotos(page: Int, limit: Int): List<Photo> =
        api.getPhotos(page, limit).map {
            Photo(it.id, it.author, it.width, it.height, it.url, it.downloadUrl)
        }
}
