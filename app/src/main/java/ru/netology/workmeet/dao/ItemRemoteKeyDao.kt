package ru.netology.workmeet.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.workmeet.entity.EventRemoteKeyEntity
import ru.netology.workmeet.entity.MyWallRemoteKeyEntity
import ru.netology.workmeet.entity.PostRemoteKeyEntity
import ru.netology.workmeet.entity.WallRemoteKeyEntity

@Dao
interface PostRemoteKeyDao {
    @Query("SELECT COUNT(*) == 0 FROM PostRemoteKeyEntity")
    suspend fun isEmpty(): Boolean

    @Query(" SELECT max('key') FROM PostRemoteKeyEntity")
    suspend fun max(): Long?

    @Query(" SELECT min('key') FROM PostRemoteKeyEntity")
    suspend fun min(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: PostRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keys: List<PostRemoteKeyEntity>)

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
    suspend fun insert(key: EventRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keys: List<EventRemoteKeyEntity>)

    @Query("DELETE FROM EventRemoteKeyEntity")
    suspend fun clear()
}

@Dao
interface WallRemoteKeyDao {
    @Query("SELECT COUNT(*) == 0 FROM WallRemoteKeyEntity")
    suspend fun isEmpty(): Boolean

    @Query(" SELECT max('key') FROM WallRemoteKeyEntity")
    suspend fun max(): Long?

    @Query(" SELECT min('key') FROM WallRemoteKeyEntity")
    suspend fun min(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: WallRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keys: List<WallRemoteKeyEntity>)

    @Query("DELETE FROM WallRemoteKeyEntity")
    suspend fun clear()
}
@Dao
interface MyWallRemoteKeyDao {
    @Query("SELECT COUNT(*) == 0 FROM MyWallRemoteKeyEntity")
    suspend fun isEmpty(): Boolean

    @Query(" SELECT max('key') FROM MyWallRemoteKeyEntity")
    suspend fun max(): Long?

    @Query(" SELECT min('key') FROM MyWallRemoteKeyEntity")
    suspend fun min(): Long?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: MyWallRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keys: List<MyWallRemoteKeyEntity>)

    @Query("DELETE FROM MyWallRemoteKeyEntity")
    suspend fun clear()
}