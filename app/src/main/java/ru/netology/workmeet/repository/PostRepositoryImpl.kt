package ru.netology.workmeet.repository

import androidx.paging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.workmeet.api.ApiService
import ru.netology.workmeet.dao.PostDao
import ru.netology.workmeet.dao.PostRemoteKeyDao
import ru.netology.workmeet.db.AppDb
import ru.netology.workmeet.dto.FeedItem
import ru.netology.workmeet.dto.Post
import ru.netology.workmeet.entity.PostEntity
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
    override suspend fun getAllP() {
        TODO("Not yet implemented")
    }
    override suspend fun likeById(id: Long) {

    }
    override suspend fun unlikeById(id: Long) {

    }
    override suspend fun save(post: Post) {

    }
    override suspend fun removeById(id: Long) {

    }
    override suspend fun saveWithAttachment(post: Post, file: File) {

    }

}