package ru.netology.workmeet.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.workmeet.api.ApiService
import ru.netology.workmeet.dao.EventDao
import ru.netology.workmeet.dao.EventRemoteKeyDao
import ru.netology.workmeet.db.AppDb
import ru.netology.workmeet.entity.*
import ru.netology.workmeet.error.ApiError

@OptIn(ExperimentalPagingApi::class)
class EventRemoteMediator (
    private val service: ApiService,
    private val db: AppDb,
    private val eventDao: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao
        ): RemoteMediator<Int, EventEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EventEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> service.getLatestE(state.config.initialLoadSize)
                LoadType.PREPEND -> {
                    val id = eventRemoteKeyDao.max() ?: return MediatorResult.Success(false)
                    service.getAfterE(id, state.config.pageSize)
                }
                LoadType.APPEND -> {
                    val id = eventRemoteKeyDao.max() ?: return MediatorResult.Success(false)
                    service.getBeforeE(id, state.config.pageSize)
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
                        eventDao.removeAll()

                        eventRemoteKeyDao.insert(
                            listOf(
                                EventRemoteKeyEntity(
                                    type = KeyType.AFTER,
                                    id = body.first().id,
                                ),
                                EventRemoteKeyEntity(
                                    type = KeyType.BEFORE,
                                    id = body.last().id,
                                ),
                            )
                        )

                    }
                    LoadType.PREPEND -> {
                        eventRemoteKeyDao.insert(
                            EventRemoteKeyEntity(
                                type = KeyType.AFTER,
                                id = body.first().id,
                            )
                        )
                    }
                    LoadType.APPEND -> {
                        eventRemoteKeyDao.insert(
                            EventRemoteKeyEntity(
                                type = KeyType.BEFORE,
                                id = body.last().id,
                            )
                        )
                    }
                }
                eventDao.insert(body.toEntity())
            }
            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

}