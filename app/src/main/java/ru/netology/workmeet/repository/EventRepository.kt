package ru.netology.workmeet.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.workmeet.dto.*

interface EventRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun likeById(id: Long)
    suspend fun unlikeById(id: Long)
    suspend fun participateById(id: Long)
    suspend fun avoidById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(event: Event, upload: MediaUpload?)
}