package ru.netology.workmeet.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.workmeet.api.ApiService
import ru.netology.workmeet.dao.MyWallRemoteKeyDao
import ru.netology.workmeet.dao.PostDao
import ru.netology.workmeet.db.AppDb
import ru.netology.workmeet.entity.*
import ru.netology.workmeet.error.ApiError

@OptIn(ExperimentalPagingApi::class)
class MyWallRemoteMediator(
    private val service: ApiService,
    private val db: AppDb,
    private val postDao: PostDao,
    private val myWallRemoteKeyDao: MyWallRemoteKeyDao,
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {

        try {
            val response = when (loadType) {
                LoadType.REFRESH -> service.getMyWallLatestP(state.config.initialLoadSize)
                LoadType.PREPEND -> {
                    val id = myWallRemoteKeyDao.max() ?: return MediatorResult.Success(true)
                    service.getMyWallAfterP(id, state.config.pageSize)
                }
                LoadType.APPEND -> {
                    val id = myWallRemoteKeyDao.min() ?: return MediatorResult.Success(true)
                    service.getMyWallBeforeP(id, state.config.pageSize)
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
                        myWallRemoteKeyDao.insert(
                            listOf(
                                MyWallRemoteKeyEntity(
                                    type = KeyType.AFTER,
                                    id = body.first().id,
                                ),
                                MyWallRemoteKeyEntity(
                                    type = KeyType.BEFORE,
                                    id = body.last().id,
                                ),
                            )
                        )

                    }
                    LoadType.PREPEND -> {
                        myWallRemoteKeyDao.insert(
                            MyWallRemoteKeyEntity(
                                type = KeyType.AFTER,
                                id = body.first().id,
                            )
                        )
                    }
                    LoadType.APPEND -> {
                        myWallRemoteKeyDao.insert(
                            MyWallRemoteKeyEntity(
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