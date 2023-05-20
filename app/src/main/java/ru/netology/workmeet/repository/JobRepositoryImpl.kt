package ru.netology.workmeet.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import okio.IOException
import ru.netology.workmeet.api.ApiService
import ru.netology.workmeet.dao.JobDao
import ru.netology.workmeet.dto.Job
import ru.netology.workmeet.entity.JobEntity
import ru.netology.workmeet.entity.toEntity
import ru.netology.workmeet.error.ApiError
import ru.netology.workmeet.error.NetworkError
import ru.netology.workmeet.error.WhoKnowsError
import javax.inject.Inject

class JobRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val jobDao: JobDao,
   ) : JobRepository {

    override val data: Flow<List<JobEntity>> = jobDao.getAllJ()
        .flowOn(Dispatchers.Default)

    override suspend fun getAllJ(id: Long) {
        try {
            val response = apiService.getAllJ(id)
            if (!response.isSuccessful) {
                throw Error(response.message())
            }
            val jobs = response.body() ?: throw Error(response.message())

            jobDao.insert(jobs.toEntity(id))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw WhoKnowsError
        }
    }

    override suspend fun getMyJobs(myId: Long) {
        try{
            val response = apiService.getAllMyJ()
            if(!response.isSuccessful){
                throw Error(response.message())
            }
            val myJobs = response.body() ?: throw Error(response.message())
            jobDao.insert(myJobs.toEntity(myId))
        } catch (e: IOException){
            throw NetworkError
        } catch (e: Exception){
            throw WhoKnowsError
        }
    }

    override suspend fun save(job: Job, id: Long) {
        try {
            val response = apiService.saveJ(job)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val newJob = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(JobEntity.fromDto(newJob, id))
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