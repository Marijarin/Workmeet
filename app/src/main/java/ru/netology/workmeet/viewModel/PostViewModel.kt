package ru.netology.workmeet.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.dto.*
import ru.netology.workmeet.model.FeedModelState
import ru.netology.workmeet.model.MediaModel
import ru.netology.workmeet.repository.PostRepository
import ru.netology.workmeet.ui.MediaLifecycleObserver
import ru.netology.workmeet.util.SingleLiveEvent
import java.io.File
import java.io.InputStream
import javax.inject.Inject

private val empty = Post(
    id = 0,
    author = " ",
    authorAvatar = " ",
    authorJob = null,
    content = "string",
    published = "now",
    link = null
)

private val noMedia = MediaModel(null, null)

private  val noUpload = MediaUpload(null, " ")


@HiltViewModel
open class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {
    private val cached = repository
        .data
        .cachedIn(viewModelScope)

    protected val mediaObserver = MediaLifecycleObserver()

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<FeedItem>> = appAuth.state.flatMapLatest { (myId, _) ->
        cached.map { pagingData ->
            pagingData.map { post ->
                if (post is Post) {
                    post.copy(ownedByMe = post.authorId == myId)
                } else post
            }
        }
    }

    private val _dataState = MutableLiveData<FeedModelState>(FeedModelState.Idle)
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _media = MutableLiveData(noMedia)
    val media: LiveData<MediaModel>
        get() = _media

    private val _upload = MutableLiveData(noUpload)
    val upload: LiveData<MediaUpload>
        get() = _upload

    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState.Loading
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
            e.printStackTrace()
        }
    }

    fun save() =
        edited.value?.let {
            viewModelScope.launch {
                try {
                    repository.save(
                        it, upload.value
                    )
                    _postCreated.value = Unit
                    _dataState.value = FeedModelState.Idle
                } catch (e: Exception) {
                    _dataState.value = FeedModelState.Error
                    e.printStackTrace()
                }

            }
            edited.value = empty
            _media.value = noMedia
            _upload.value = noUpload

        }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState.Refreshing
            repository.likeById(id)
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun unlikeById(id: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState.Refreshing
            repository.unlikeById(id)
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState.Refreshing
            repository.removeById(id)
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun changeAttachment(uri: Uri?, file: File?) {
        _media.value = MediaModel(uri, file)
    }

    fun changeFile(uri: Uri, inputStream: InputStream){
        val name = uri.pathSegments.last().substringAfterLast('/')
        _upload.value = MediaUpload(inputStream, name)
    }


    fun playMedia(post: Post) {
        when (post.attachment?.typeA) {
            AttachmentType.AUDIO -> {
                mediaObserver.apply {
                    if (player != null && player!!.isPlaying) {
                        player?.stop()
                        player?.reset()
                        player?.setDataSource(
                            post.attachment.url
                        )
                    } else {
                        player?.setDataSource(
                            post.attachment.url
                        )
                    }
                }.play()
            }
            AttachmentType.VIDEO -> {
                mediaObserver.apply {
                    if (player != null && player!!.isPlaying) {
                        player?.stop()
                        player?.reset()
                    }
                }
            }
            else -> Unit
        }
    }

    fun pause() {
        mediaObserver.apply {
            if (player != null) {
                player?.stop()
                player?.reset()
            }
        }
    }
}