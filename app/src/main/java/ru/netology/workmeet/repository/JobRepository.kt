package ru.netology.workmeet.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.workmeet.dto.FeedItem
import ru.netology.workmeet.dto.Job

interface JobRepository {
    val data: Flow<List<Job?>>
    suspend fun getAllJ(id: Long)
    suspend fun save(job: Job)
    suspend fun removeById(id: Long)
}