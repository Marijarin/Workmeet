package ru.netology.workmeet.repository

import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import ru.netology.workmeet.api.ApiService
import ru.netology.workmeet.dao.*
import ru.netology.workmeet.db.AppDb
import ru.netology.workmeet.dto.*
import ru.netology.workmeet.entity.JobEntity
import ru.netology.workmeet.entity.PostEntity
import ru.netology.workmeet.entity.UserEntity
import ru.netology.workmeet.error.ApiError
import ru.netology.workmeet.error.NetworkError
import ru.netology.workmeet.error.WhoKnowsError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val postDao: PostDao,
    private val userDao: UserDao,
    private val jobDao: JobDao,
    postRemoteKeyDao: PostRemoteKeyDao,
    private val wallRemoteKeyDao: WallRemoteKeyDao,
    private val myWallRemoteKeyDao: MyWallRemoteKeyDao,
    private val appDb: AppDb,
) : PostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 5, enablePlaceholders = false),
        remoteMediator = PostRemoteMediator(apiService, appDb, postDao, postRemoteKeyDao),
        pagingSourceFactory = postDao::pagingSource
    ).flow.map { pagingData ->
        pagingData.map(PostEntity::toDto)
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun loadUserWall(
        authorId: Long,
        appDb: AppDb,
        wallRemoteKeyDao: WallRemoteKeyDao
    ): Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 5, enablePlaceholders = false),
        remoteMediator = WallRemoteMediator(apiService, appDb, postDao, wallRemoteKeyDao, authorId),
        pagingSourceFactory = postDao::pagingSource
    ).flow.map { pagingData ->
        pagingData
            .map(PostEntity::toDto)
    }


    @OptIn(ExperimentalPagingApi::class)
    override fun loadMyWall(
        authorId: Long,
        appDb: AppDb,
        myWallRemoteKeyDao: MyWallRemoteKeyDao
    ): Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 5, enablePlaceholders = false),
        remoteMediator = MyWallRemoteMediator(apiService, appDb, postDao, myWallRemoteKeyDao),
        pagingSourceFactory = postDao::pagingSource
    ).flow.map { pagingData ->
        pagingData
            .map(PostEntity::toDto)
    }


    override suspend fun likeById(id: Long) {
        try {
            val response = apiService.likeByIdP(id)
            postDao.likeById(id)
            if (response.body() != null && response.isSuccessful) {
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
            val response = apiService.unlikeByIdP(id)
            postDao.likeById(id)
            if (response.body() != null && response.isSuccessful) {
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

    suspend fun upload(upload: MediaUpload): Media {

        try {
            val name = upload.uri?.pathSegments?.last()?.substringAfterLast('/')
            val media = upload.inputStream?.readBytes()
                ?.toRequestBody("multipart/form-data".toMediaType()).let {
                    MultipartBody.Part.createFormData(
                        "file",
                        name,
                        upload.uri.toString().toRequestBody()
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


    override suspend fun save(post: Post, upload: MediaUpload?) {
        try {
            val media = if (upload?.inputStream != null) upload(upload) else null

    val postWithAttachment = when (upload?.uploadType) {
        "image/*" -> post.copy(
            attachment = media?.let {
                Attachment(
                    it.url,
                    AttachmentType.IMAGE
                )
            }
        )
        "audio/*" -> {
            post.copy(attachment = media?.let { Attachment(it.url, AttachmentType.AUDIO) })
        }
        "video/*" -> post.copy(
            attachment = media?.let {
                Attachment(
                    it.url,
                    AttachmentType.VIDEO
                )
            }
        )
        else -> post
    }

            val response = apiService.saveP(postWithAttachment)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: java.io.IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw WhoKnowsError
        }
    }

    override suspend fun getUserById(userId: Long): User {
        try {
            val response = apiService.getUserById(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            userDao.insert(UserEntity.fromDto(body))
            return body
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw WhoKnowsError
        }
    }

    override suspend fun getUsersLastJob(userId: Long): Job {
        try {
            val response = apiService.getAllJ(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(body.map { JobEntity.fromDto(it, userId) })
            return body.last()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw WhoKnowsError
        }
    }

    override fun getDb(): AppDb = appDb
    override fun getRK(): WallRemoteKeyDao = wallRemoteKeyDao
    override fun getMyRK(): MyWallRemoteKeyDao = myWallRemoteKeyDao


}