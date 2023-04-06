package ru.netology.workmeet.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.workmeet.api.ApiService
import ru.netology.workmeet.dao.PostDao
import ru.netology.workmeet.dao.PostRemoteKeyDao
import ru.netology.workmeet.db.AppDb
import ru.netology.workmeet.entity.KeyType
import ru.netology.workmeet.entity.PostEntity
import ru.netology.workmeet.entity.PostRemoteKeyEntity
import ru.netology.workmeet.entity.toEntity
import ru.netology.workmeet.error.ApiError

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator (private val service: ApiService,
                          private val db: AppDb,
                          private val postDao: PostDao,
                          private val postRemoteKeyDao: PostRemoteKeyDao,
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> service.getLatestP(state.config.initialLoadSize)
                LoadType.PREPEND -> {
                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(false)
                    service.getAfterP(id, state.config.pageSize)
                }
                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(false)
                    service.getBeforeP(id, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message(),
            )

            db.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        postDao.removeAll()

                        postRemoteKeyDao.insert(
                            listOf(
                                PostRemoteKeyEntity(
                                    type = KeyType.AFTER,
                                    id = body.first().id,
                                ),
                                PostRemoteKeyEntity(
                                    type = KeyType.BEFORE,
                                    id = body.last().id,
                                ),
                            )
                        )

                    }
                    LoadType.PREPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                type = KeyType.AFTER,
                                id = body.first().id,
                            )
                        )
                    }
                    LoadType.APPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                type = KeyType.BEFORE,
                                id = body.last().id,
                            )
                        )
                    }
                }
                postDao.insert(body.toEntity())
            }
            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}