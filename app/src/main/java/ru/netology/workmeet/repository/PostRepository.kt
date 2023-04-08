package ru.netology.workmeet.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.workmeet.dto.AttachmentType
import ru.netology.workmeet.dto.FeedItem
import ru.netology.workmeet.dto.Post
import java.io.File

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun getAllP()
    suspend fun likeById(id: Long)
    suspend fun unlikeById(id: Long)
    suspend fun save(post: Post)
    suspend fun removeById(id: Long)
    suspend fun saveWithAttachment(post: Post, file: File, type: AttachmentType)

}
