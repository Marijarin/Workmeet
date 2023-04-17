package ru.netology.workmeet.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.workmeet.dto.Job

interface JobRepository {
    val data: Flow<List<Job?>>
    suspend fun getAllJ(id: Long)
    suspend fun save(job: Job, id: Long)
    suspend fun removeById(id: Long)
}