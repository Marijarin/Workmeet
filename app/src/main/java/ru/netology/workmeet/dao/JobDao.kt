package ru.netology.workmeet.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.workmeet.dto.UserPreview
import ru.netology.workmeet.entity.JobEntity

@Dao
interface JobDao {
    @Query("SELECT * FROM JobEntity ORDER BY id DESC")
    fun getAll(): Flow<List<JobEntity>>
    @Query("SELECT * FROM JobEntity ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, JobEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jobs: List<JobEntity>)

    @Query("UPDATE JobEntity SET finish = :finish WHERE id = :id")
    suspend fun updateFinishById(id: Long, finish: String)

    suspend fun save(job: JobEntity) =
        if (job.id == 0L) insert(job) else job.finish?.let { updateFinishById(job.id, it) }

    @Query("""
        UPDATE PostEntity SET
        likeOwnerIds = :likeOwnerIds ,
        users = :users,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """)
    suspend fun likeById(id: Long, likeOwnerIds: List<Long>, users: List<UserPreview>)

    @Query("DELETE FROM JobEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM JobEntity")
    suspend fun removeAll()
}