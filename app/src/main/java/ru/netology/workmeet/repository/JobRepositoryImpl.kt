package ru.netology.workmeet.repository

import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.workmeet.api.ApiService
import ru.netology.workmeet.dao.JobDao
import ru.netology.workmeet.dao.JobRemoteKeyDao
import ru.netology.workmeet.db.AppDb
import ru.netology.workmeet.dto.FeedItem
import ru.netology.workmeet.dto.Job
import ru.netology.workmeet.dto.Post
import ru.netology.workmeet.entity.JobEntity
import java.io.File
import javax.inject.Inject

class JobRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val jobDao: JobDao,
    jobRemoteKeyDao: JobRemoteKeyDao,
    appDb: AppDb,
) : JobRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 10),
        remoteMediator = JobRemoteMediator(apiService, appDb, jobDao, jobRemoteKeyDao),
        pagingSourceFactory = jobDao::pagingSource
    ).flow.map { pagingData ->
        pagingData.map(JobEntity::toDto)
    }

    override suspend fun getAllJ() {
        TODO("Not yet implemented")
    }

    override suspend fun updateFinishById(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun save(job: Job) {

    }
    override suspend fun removeById(id: Long) {

    }

}