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
import ru.netology.workmeet.repository.EventRepository
import ru.netology.workmeet.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

private val empty = Event(
    id = 0L,
    author = "",
    authorAvatar = "",
    authorJob = "",
    content = "",
    datetime = "",
    published = "",
    coords = null,
    type = EventType.EMPTY,
    link = "",
    attachment = null
)

private val noMedia = MediaModel(null, null)
@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    appAuth: AppAuth
): ViewModel() {
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

    init {
        loadEvents()
    }

    fun loadEvents() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState.Loading
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }

    fun save() = viewModelScope.launch{
        edited.value?.let {
            _eventCreated.value = Unit
            viewModelScope.launch {
                try {
                    when(_media.value){
                        noMedia ->  repository.save(it)
                        else -> _media.value?.file?.let{ file ->
                            repository.saveWithAttachment(it, file, type = AttachmentType.IMAGE)
                        }
                    }
                    _dataState.value = FeedModelState.Idle
                } catch (e: Exception) {
                    _dataState.value = FeedModelState.Error
                }
            }
        }
        edited.value = empty
    }

    fun edit(event: Event) {
        edited.value = event
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
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


}