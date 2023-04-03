package ru.netology.workmeet.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.workmeet.entity.UserPreviewEntity

@Dao
interface UserPreviewDao {
    @Query("SELECT * FROM UserPreviewEntity ORDER BY userId DESC")
    fun getAll(): Flow<List<UserPreviewEntity>>
    @Query("SELECT * FROM UserPreviewEntity ORDER BY userId DESC")
    fun pagingSource(): PagingSource<Int, UserPreviewEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userPreview: UserPreviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userPreviews: List<UserPreviewEntity>)

    @Query("DELETE FROM UserPreviewEntity WHERE userId = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM UserPreviewEntity")
    suspend fun removeAll()
}