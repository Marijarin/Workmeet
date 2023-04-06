package ru.netology.workmeet.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.workmeet.dto.Event
import ru.netology.workmeet.dto.FeedItem
import ru.netology.workmeet.dto.Post

interface EventRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun getAllE()
    suspend fun participateById(id: Long)
    suspend fun avoidById(id: Long)
    suspend fun save(event: Event)
    suspend fun removeById(id: Long)
}