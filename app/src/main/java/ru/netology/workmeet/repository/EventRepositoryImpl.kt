package ru.netology.workmeet.repository

import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.workmeet.api.ApiService
import ru.netology.workmeet.dao.EventDao
import ru.netology.workmeet.dao.EventRemoteKeyDao
import ru.netology.workmeet.db.AppDb
import ru.netology.workmeet.dto.Event
import ru.netology.workmeet.dto.FeedItem
import ru.netology.workmeet.entity.EventEntity
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
    override suspend fun getAllE() {
        TODO("Not yet implemented")
    }

    override suspend fun participateById(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun avoidById(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun save(event: Event) {
        TODO("Not yet implemented")
    }
    override suspend fun removeById(id: Long) {

    }
}