package ru.netology.workmeet.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okio.IOException
import ru.netology.workmeet.api.ApiService
import ru.netology.workmeet.dao.JobDao
import ru.netology.workmeet.db.AppDb
import ru.netology.workmeet.dto.FeedItem
import ru.netology.workmeet.dto.Job
import ru.netology.workmeet.entity.JobEntity
import ru.netology.workmeet.entity.PostEntity
import ru.netology.workmeet.entity.toEntity
import ru.netology.workmeet.error.ApiError
import ru.netology.workmeet.error.NetworkError
import ru.netology.workmeet.error.WhoKnowsError
import javax.inject.Inject

class JobRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val jobDao: JobDao,
    appDb: AppDb,
) : JobRepository {
    private val _data = MutableStateFlow<List<Job?>>(emptyList())
    override val data: Flow<List<Job?>>
    get() = _data.asStateFlow()

    override suspend fun getAllJ(id: Long) {
        try {
            val response = apiService.getAllJ(id)
            if (!response.isSuccessful) {
                throw Error(response.message())
            }
            val jobs = response.body() ?: throw Error(response.message())
            _data.value = jobs
            jobDao.insert(jobs.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw WhoKnowsError
        }
    }
    override suspend fun save(job: Job) {
        try {
            val response = apiService.saveJ(job)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val newJob = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(JobEntity.fromDto(newJob))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw WhoKnowsError
        }
    }
    override suspend fun removeById(id: Long) {
        try {
            val response = apiService.removeByIdJ(id)
            jobDao.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw WhoKnowsError
        }
    }

}