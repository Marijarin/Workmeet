package ru.netology.workmeet.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.workmeet.dao.*
import ru.netology.workmeet.entity.*

@Database(
    entities = [
        PostEntity::class,
        PostRemoteKeyEntity::class,
        EventEntity::class,
        EventRemoteKeyEntity::class,
        JobEntity::class,
        UserEntity::class],
    version = 1, exportSchema = false
)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun eventDao(): EventDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
    abstract fun jobDao(): JobDao
    abstract fun userDao(): UserDao
    abstract fun wallRemoteKeyDao(): WallRemoteKeyDao

}