package ru.netology.workmeet.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.workmeet.dto.Job
import ru.netology.workmeet.entity.JobEntity

interface JobRepository {
    val data: Flow<List<JobEntity>>
    suspend fun getAllJ(id: Long)
    suspend fun save(job: Job, id: Long)
    suspend fun removeById(id: Long)

    suspend fun getMyJobs(myId:Long)
}