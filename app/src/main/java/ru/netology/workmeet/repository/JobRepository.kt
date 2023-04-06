package ru.netology.workmeet.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.workmeet.dto.Event
import ru.netology.workmeet.dto.FeedItem

interface JobRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun getAllJ()
    suspend fun updateFinishById(id: Long)
    suspend fun save(job: Job)
    suspend fun removeById(id: Long)
}