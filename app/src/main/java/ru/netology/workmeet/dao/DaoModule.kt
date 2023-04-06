package ru.netology.workmeet.dao

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.workmeet.db.AppDb

@InstallIn(SingletonComponent::class)
@Module
object DaoModule {
    @Provides
    fun providePostDao(db: AppDb): PostDao = db.postDao()
    @Provides
    fun providePostRemoteKeyDao(db: AppDb): PostRemoteKeyDao = db.postRemoteKeyDao()
    @Provides
    fun provideEventDao(db: AppDb): EventDao = db.eventDao()
    @Provides
    fun provideEventRemoteKeyDao(db: AppDb): EventRemoteKeyDao = db.eventRemoteKeyDao()
    @Provides
    fun provideJobDao(db: AppDb): JobDao = db.jobDao()
    @Provides
    fun provideJobRemoteKeyDao(db: AppDb): JobRemoteKeyDao = db.jobRemoteKeyDao()
    @Provides
    fun provideUserDao(db: AppDb): UserDao = db.userDao()
    @Provides
    fun provideUserPreviewDao(db: AppDb): UserPreviewDao = db.userPreviewDao()

}
