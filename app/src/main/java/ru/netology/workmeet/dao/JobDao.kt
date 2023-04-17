package ru.netology.workmeet.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.workmeet.entity.JobEntity

@Dao
interface JobDao {
    @Query("SELECT * FROM JobEntity ORDER BY id DESC")
    fun getAllJ(): Flow<List<JobEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jobs: List<JobEntity>)

    @Query("DELETE FROM JobEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM JobEntity")
    suspend fun removeAll()
}