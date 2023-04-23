package ru.netology.workmeet.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.workmeet.api.ApiService
import ru.netology.workmeet.dao.PostDao
import ru.netology.workmeet.dao.WallRemoteKeyDao
import ru.netology.workmeet.db.AppDb
import ru.netology.workmeet.entity.*
import ru.netology.workmeet.error.ApiError

@OptIn(ExperimentalPagingApi::class)
class WallRemoteMediator (private val service: ApiService,
                          private val db: AppDb,
                          private val postDao: PostDao,
                          private val wallRemoteKeyDao: WallRemoteKeyDao,
                          private val authorId: Long
): RemoteMediator<Int, PostEntity>(){
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {

        try {
            val response = when (loadType) {
                LoadType.REFRESH -> service.getUserWallLatestP(authorId, state.config.initialLoadSize)
                LoadType.PREPEND -> {
                    val id = wallRemoteKeyDao.max() ?: return MediatorResult.Success(false)
                    service.getUserWallAfterP(authorId, id, state.config.pageSize)
                }
                LoadType.APPEND -> {
                    val id = wallRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    service.getUserWallBeforeP(authorId, id, state.config.pageSize)
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
                        wallRemoteKeyDao.insert(
                            listOf(
                                WallRemoteKeyEntity(
                                    type = KeyType.AFTER,
                                    id = body.first().id,
                                ),
                                WallRemoteKeyEntity(
                                    type = KeyType.BEFORE,
                                    id = body.last().id,
                                ),
                            )
                        )

                    }
                    LoadType.PREPEND -> {
                        wallRemoteKeyDao.insert(
                            WallRemoteKeyEntity(
                                type = KeyType.AFTER,
                                id = body.first().id,
                            )
                        )
                    }
                    LoadType.APPEND -> {
                        wallRemoteKeyDao.insert(
                            WallRemoteKeyEntity(
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