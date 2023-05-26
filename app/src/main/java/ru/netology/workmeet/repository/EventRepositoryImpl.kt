package ru.netology.workmeet.repository

import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import ru.netology.workmeet.api.ApiService
import ru.netology.workmeet.dao.EventDao
import ru.netology.workmeet.dao.EventRemoteKeyDao
import ru.netology.workmeet.db.AppDb
import ru.netology.workmeet.dto.*
import ru.netology.workmeet.entity.EventEntity
import ru.netology.workmeet.error.ApiError
import ru.netology.workmeet.error.NetworkError
import ru.netology.workmeet.error.WhoKnowsError
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val eventDao: EventDao,
    eventRemoteKeyDao: EventRemoteKeyDao,
    appDb: AppDb,
) : EventRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 5),
        remoteMediator = EventRemoteMediator(apiService, appDb, eventDao, eventRemoteKeyDao),
        pagingSourceFactory = eventDao::pagingSource
    ).flow.map { pagingData ->
        pagingData.map(EventEntity::toDto)
    }
    override suspend fun likeById(id: Long) {
        try {
            val response = apiService.likeByIdE(id)
            eventDao.likeById(id)
            if(response.isSuccessful) {
                val updated = EventEntity.fromDto(response.body()!!)
                eventDao.insert(updated)
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw WhoKnowsError
        }
    }
    override suspend fun unlikeById(id: Long) {
        try {
            val response = apiService.likeByIdE(id)
            eventDao.likeById(id)
            if(response.isSuccessful) {
                val updated = EventEntity.fromDto(response.body()!!)
                eventDao.insert(updated)
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw WhoKnowsError
        }
    }
    override suspend fun participateById(id: Long) {
        try {
            val response = apiService.participateById(id)
            eventDao.participateById(id)
            if(response.isSuccessful) {
                val updated = EventEntity.fromDto(response.body()!!)
                eventDao.insert(updated)
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw WhoKnowsError
        }
    }

    override suspend fun avoidById(id: Long) {
        try {
            val response = apiService.participateById(id)
            eventDao.participateById(id)
            if(response.isSuccessful) {
                val updated = EventEntity.fromDto(response.body()!!)
                eventDao.insert(updated)
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw WhoKnowsError
        }
    }


    override suspend fun removeById(id: Long) {
        try {
            val response = apiService.removeByIdE(id)
            eventDao.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw WhoKnowsError
        }
    }
    suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = upload.inputStream?.readBytes()
                ?.toRequestBody("multipart/form-data".toMediaType()).let {
                    MultipartBody.Part.createFormData(
                        "file",
                        upload.name,
                        it ?: upload.name.toRequestBody()
                    )
                }
            val response = apiService.upload(media)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())

        } catch (e: java.io.IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw WhoKnowsError
        }
    }


    override suspend fun save(event: Event, upload: MediaUpload?) {
        try {
            val eventWithAttachment = upload?.let {
                upload(it)
            }?.let {
                when {
                    it.url.contains(".png") || it.url.contains(".jpeg") -> event.copy(
                        attachment = Attachment(
                            it.url,
                            AttachmentType.IMAGE
                        ), ownedByMe = true
                    )
                    it.url.contains(".mp3") || it.url.contains(".flac") || it.url.contains(".wav") || it.url.contains(".ogg") -> {
                        event.copy(attachment = Attachment(it.url, AttachmentType.AUDIO), ownedByMe = true)
                    }
                    it.url.contains(".mp4") -> event.copy(
                        attachment = Attachment(
                            it.url,
                            AttachmentType.VIDEO
                        )
                    )
                    else -> event.copy(attachment = null, ownedByMe = true)
                }
            }
            val response = apiService.saveE(eventWithAttachment ?: event)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: java.io.IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw WhoKnowsError
        }
    }
}