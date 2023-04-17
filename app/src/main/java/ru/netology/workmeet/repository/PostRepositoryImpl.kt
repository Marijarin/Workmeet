package ru.netology.workmeet.repository

import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import ru.netology.workmeet.api.ApiService
import ru.netology.workmeet.dao.PostDao
import ru.netology.workmeet.dao.PostRemoteKeyDao
import ru.netology.workmeet.db.AppDb
import ru.netology.workmeet.dto.*
import ru.netology.workmeet.entity.PostEntity
import ru.netology.workmeet.error.ApiError
import ru.netology.workmeet.error.NetworkError
import ru.netology.workmeet.error.WhoKnowsError
import java.io.File
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val postDao: PostDao,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb,
) : PostRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 10),
        remoteMediator = PostRemoteMediator(apiService, appDb, postDao, postRemoteKeyDao),
        pagingSourceFactory = postDao::pagingSource
    ).flow.map { pagingData ->
        pagingData.map(PostEntity::toDto)
    }

    override suspend fun likeById(id: Long) {
        try {
            val response = apiService.likeByIdP(id)
            postDao.likeById(id)
            if (response.body()!=null && response.isSuccessful) {
                val post: Post = response.body()!!
                postDao.insert(PostEntity.fromDto(post))
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
            val response = apiService.likeByIdP(id)
            postDao.likeById(id)
            if (response.body()!=null && response.isSuccessful) {
                val post: Post = response.body()!!
                postDao.insert(PostEntity.fromDto(post))
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

    override suspend fun save(post: Post) {
        try {
            val response = apiService.saveP(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val newPost = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(newPost))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw WhoKnowsError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            val response = apiService.removeByIdP(id)
            postDao.removeById(id)
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

    override suspend fun saveWithAttachment(post: Post, file: File, type: AttachmentType) {
        try {
            val upload = upload(file)
            val postWithAttachment = post.copy(attachment = Attachment(upload.id, type))
            save(postWithAttachment)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw WhoKnowsError
        }
    }

}