package ru.netology.workmeet.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.workmeet.entity.EventRemoteKeyEntity
import ru.netology.workmeet.entity.JobRemoteKeyEntity
import ru.netology.workmeet.entity.PostRemoteKeyEntity

@Dao
interface PostRemoteKeyDao {

    @Query(" SELECT max('key') FROM PostRemoteKeyEntity")
    suspend fun max(): Long?

    @Query(" SELECT min('key') FROM PostRemoteKeyEntity")
    suspend fun min(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(postRemoteKeyEntity: PostRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(postRemoteKeyEntity: List<PostRemoteKeyEntity>)

    @Query("DELETE FROM PostRemoteKeyEntity")
    suspend fun clear()
}

@Dao
interface EventRemoteKeyDao {

    @Query(" SELECT max('key') FROM EventRemoteKeyEntity")
    suspend fun max(): Long?

    @Query(" SELECT min('key') FROM EventRemoteKeyEntity")
    suspend fun min(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(eventRemoteKeyEntity: EventRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(eventRemoteKeyEntity: List<EventRemoteKeyEntity>)

    @Query("DELETE FROM EventRemoteKeyEntity")
    suspend fun clear()
}

@Dao
interface JobRemoteKeyDao {

    @Query(" SELECT max('key') FROM JobRemoteKeyEntity")
    suspend fun max(): Long?

    @Query(" SELECT min('key') FROM JobRemoteKeyEntity")
    suspend fun min(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jobRemoteKeyEntity: JobRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jobRemoteKeyEntity: List<JobRemoteKeyEntity>)

    @Query("DELETE FROM JobRemoteKeyEntity")
    suspend fun clear()
}