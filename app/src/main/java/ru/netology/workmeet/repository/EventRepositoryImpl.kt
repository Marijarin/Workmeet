package ru.netology.workmeet.repository

import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
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
import java.io.File
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val eventDao: EventDao,
    eventRemoteKeyDao: EventRemoteKeyDao,
    appDb: AppDb,
) : EventRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 10),
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

    override suspend fun save(event: Event) {
        try {
            val response = apiService.saveE(event)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val newEvent = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(newEvent))
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
    private suspend fun upload(file: File): Media {
        try {
            val data = MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody()
            )
            val response = apiService.upload(data)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw WhoKnowsError
        }
    }

    override suspend fun saveWithAttachment(event: Event, file: File, type: AttachmentType) {
        try {
            val upload = upload(file)
            val eventWithAttachment = event.copy(
                //attachment = Attachment(upload.id, type)
                )
            save(eventWithAttachment)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw WhoKnowsError
        }
    }
}