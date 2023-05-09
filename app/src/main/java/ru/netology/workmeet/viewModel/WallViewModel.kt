package ru.netology.workmeet.viewModel

import androidx.core.net.toFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.dto.AttachmentType
import ru.netology.workmeet.dto.MediaUpload
import ru.netology.workmeet.dto.Post
import ru.netology.workmeet.dto.User
import ru.netology.workmeet.model.FeedModelState
import ru.netology.workmeet.model.MediaModel
import ru.netology.workmeet.repository.PostRepository
import ru.netology.workmeet.ui.MediaLifecycleObserver
import ru.netology.workmeet.util.SingleLiveEvent
import javax.inject.Inject

private val empty = Post(
    id = 0,
    author = " ",
    authorAvatar = " ",
    authorJob = null,
    content = "string",
    published = "now",
    link = null,
    attachment = null
)
private val typical = User(
    id = 0,
    avatar = null,
    login = "nouser",
    name = "Nouser"
)

private val noMedia = MediaModel(null, null)

@HiltViewModel
class WallViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {
    val userId = MutableStateFlow(0L)
    fun setUserId(id: Long) {
        userId.run {
            this.value = id
        }
    }

    private val mediaObserver = MediaLifecycleObserver()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uData: Flow<PagingData<Post>> = userId.flatMapLatest {
        repository.loadUserWall(it, repository.getDb(), repository.getRK())
    }.cachedIn(viewModelScope)


    @OptIn(ExperimentalCoroutinesApi::class)
    val myData: Flow<PagingData<Post>> = appAuth.state.flatMapLatest { (myId, _) ->
        repository.loadMyWall(myId, repository.getDb(), repository.getMyRK())
    }.cachedIn(viewModelScope)
        .map {pagingData ->
            pagingData.map{
                it.copy(ownedByMe = true)
            }

        }

    private val _dataState = MutableLiveData<FeedModelState>(FeedModelState.Idle)
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _media = MutableLiveData(noMedia)
    val media: LiveData<MediaModel>
        get() = _media

    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _user = MutableStateFlow(typical)
    val user: Flow<User>
        get() = _user

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState.Loading
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun save() =
        edited.value?.let {
            viewModelScope.launch {


                try {
                    repository.save(
                        it, _media.value?.uri?.let { MediaUpload(it.toFile()) }
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

    fun getUserById(userId: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState.Refreshing
            _user.value = repository.getUserById(userId)
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }


    fun playAudio(post: Post) {
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