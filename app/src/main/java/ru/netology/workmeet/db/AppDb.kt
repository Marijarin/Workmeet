package ru.netology.workmeet.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.workmeet.dao.*
import ru.netology.workmeet.entity.*

@Database(entities = [
    PostEntity::class,
    EventEntity::class,
    JobEntity::class,
    UserEntity::class,
    UserPreviewEntity::class],
    version = 1, exportSchema = true)
abstract class AppDb: RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun eventDao(): EventDao
    abstract fun jobDao(): JobDao
    abstract fun userDao(): UserDao
    abstract fun userPreviewDao(): UserPreviewDao
}