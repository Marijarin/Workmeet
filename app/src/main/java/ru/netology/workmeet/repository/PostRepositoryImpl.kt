package ru.netology.workmeet.repository

import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import ru.netology.workmeet.api.ApiService
import ru.netology.workmeet.dao.*
import ru.netology.workmeet.db.AppDb
import ru.netology.workmeet.dto.*
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
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

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
            val postWithAttachment = upload?.let {
                upload(it)
            }?.let {
                when {
                    it.url.contains(".png") || it.url.contains(".jpeg") -> post.copy(
                        attachment = Attachment(
                            it.url,
                            AttachmentType.IMAGE
                        )
                    )
                    it.url.contains(".mp3") || it.url.contains(".flac") || it.url.contains(".wav") -> post.copy(
                        attachment = Attachment(it.url, AttachmentType.AUDIO)
                    )
                    it.url.contains(".mp4") -> post.copy(
                        attachment = Attachment(
                            it.url,
                            AttachmentType.VIDEO
                        )
                    )
                    else -> post.copy(attachment = null)
                }
            }
            val response = apiService.saveP(postWithAttachment ?: post)
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

    override fun getDb(): AppDb = appDb
    override fun getRK(): WallRemoteKeyDao = wallRemoteKeyDao
    override fun getMyRK(): MyWallRemoteKeyDao = myWallRemoteKeyDao


}