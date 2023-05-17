package ru.netology.workmeet.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.RequestBody
import ru.netology.workmeet.dao.MyWallRemoteKeyDao
import ru.netology.workmeet.dao.WallRemoteKeyDao
import ru.netology.workmeet.db.AppDb
import ru.netology.workmeet.dto.*
import java.io.File
import java.io.InputStream

interface PostRepository {

   val data: Flow<PagingData<FeedItem>>
    fun loadUserWall(
        authorId: Long,
        appDb: AppDb,
        wallRemoteKeyDao: WallRemoteKeyDao
    ): Flow<PagingData<Post>>

    fun loadMyWall(
        authorId: Long,
        appDb: AppDb,
        myWallRemoteKeyDao: MyWallRemoteKeyDao
    ): Flow<PagingData<Post>>
    fun getDb(): AppDb

    fun getRK(): WallRemoteKeyDao

    fun getMyRK(): MyWallRemoteKeyDao

    suspend fun likeById(id: Long)
    suspend fun unlikeById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post, upload: MediaUpload?)

    suspend fun getUserById(userId: Long): User

}
