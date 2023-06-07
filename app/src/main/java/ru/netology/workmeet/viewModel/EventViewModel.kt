package ru.netology.workmeet.viewModel

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
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
import okhttp3.MediaType
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.dto.*
import ru.netology.workmeet.model.FeedModelState
import ru.netology.workmeet.model.MediaModel
import ru.netology.workmeet.repository.EventRepository
import ru.netology.workmeet.ui.MediaLifecycleObserver
import ru.netology.workmeet.util.SingleLiveEvent
import java.io.File
import java.io.InputStream
import javax.inject.Inject

private val empty = Event(
    id = 0,
    author = " ",
    authorAvatar = " ",
    authorJob = null,
    content = "string",
    datetime = " ",
    published = "now",
    coords = null,
    type = EventType.EMPTY,
    link = null,
    attachment = null
)

private val noMedia = MediaModel(null, null)


private  val noUpload = MediaUpload(null, null, null)
@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    appAuth: AppAuth
): ViewModel() {
    private val mediaObserver = MediaLifecycleObserver()
    private val cached = repository
        .data
        .cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<FeedItem>> = appAuth.state.flatMapLatest { (myId, _) ->
        cached.map { pagingData ->
            pagingData.map { event ->
                if (event is Event) {
                    event.copy(ownedByMe = event.authorId == myId)
                } else event
            }
        }
    }

    private val _dataState = MutableLiveData<FeedModelState>(FeedModelState.Idle)
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _media = MutableLiveData(noMedia)
    val media: LiveData<MediaModel>
        get() = _media

    val edited = MutableLiveData(empty)
    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

    private val _upload = MutableLiveData(noUpload)
    val upload: LiveData<MediaUpload>
        get() = _upload

    fun loadEvents() = viewModelScope.launch {
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
                        it, upload.value
                    )
                    _eventCreated.value = Unit
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
    fun edit(event: Event) {
        edited.value = event
    }

    fun changeContent(content:String, type: EventType, datetime: String){
        val contentE = content.trim()
        val datetimeE = datetime.trim()
        if (edited.value?.content == contentE
            && edited.value?.type == type
            && edited.value?.datetime == datetimeE
        ) {
            return
        }
        edited.value = edited.value?.copy(content = contentE, type = type, datetime = datetimeE)
    }
    fun speakUser(mentionedUser: Long?) {
        if (mentionedUser!=null)
            edited.value = edited.value
                ?.copy(speakerIds = listOf(mentionedUser))
    }
    fun likeById(id: Long) = viewModelScope.launch{
        try {
            _dataState.value = FeedModelState.Refreshing
            repository.likeById(id)
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }
    fun unlikeById(id: Long) = viewModelScope.launch{
        try {
            _dataState.value = FeedModelState.Refreshing
            repository.unlikeById(id)
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }
    fun participateById(id: Long) = viewModelScope.launch{
        try {
            _dataState.value = FeedModelState.Refreshing
            repository.participateById(id)
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }
    fun aviodById(id: Long) = viewModelScope.launch{
        try {
            _dataState.value = FeedModelState.Refreshing
            repository.avoidById(id)
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun removeById(id: Long) = viewModelScope.launch{
        try {
            _dataState.value = FeedModelState.Refreshing
            repository.removeById(id)
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _media.value = MediaModel(uri, file)
    }
    fun changeFile(uri: Uri, inputStream: InputStream?, type: String){
        val name = uri.pathSegments.last().substringAfterLast('/')
        _upload.value = MediaUpload(uri, inputStream, type)
    }
    fun playMedia(event: Event) {
        when (event.attachment?.typeA) {
            AttachmentType.AUDIO -> {
                mediaObserver.apply {
                    if (player != null && player!!.isPlaying) {
                        player?.stop()
                        player?.reset()
                        player?.setDataSource(
                            event.attachment.url
                        )
                    } else {
                        player?.setDataSource(
                            event.attachment.url
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


