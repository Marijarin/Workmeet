package ru.netology.workmeet.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.workmeet.dto.AttachmentType
import ru.netology.workmeet.dto.Event
import ru.netology.workmeet.dto.FeedItem
import ru.netology.workmeet.dto.Post
import java.io.File

interface EventRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun getAllE()
    suspend fun likeById(id: Long)
    suspend fun unlikeById(id: Long)
    suspend fun participateById(id: Long)
    suspend fun avoidById(id: Long)
    suspend fun save(event: Event)
    suspend fun removeById(id: Long)
    suspend fun saveWithAttachment(event: Event, file: File, type: AttachmentType)
}