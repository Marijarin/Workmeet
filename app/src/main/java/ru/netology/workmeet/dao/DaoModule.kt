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
    fun provideEventDao(db: AppDb): EventDao = db.eventDao()
    @Provides
    fun provideJobDao(db: AppDb): JobDao = db.jobDao()
    @Provides
    fun provideUserDao(db: AppDb): UserDao = db.userDao()
    @Provides
    fun provideUserPreviewDao(db: AppDb): UserPreviewDao = db.userPreviewDao()

}
