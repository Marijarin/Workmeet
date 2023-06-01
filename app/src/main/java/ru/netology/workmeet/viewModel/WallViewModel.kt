package ru.netology.workmeet.viewModel

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
import ru.netology.workmeet.dto.*
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


@HiltViewModel
class WallViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
) : PostViewModel(repository, appAuth) {

    val userId = MutableStateFlow(0L)
    fun setUserId(id: Long) {
        userId.run {
            this.value = id
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uData: Flow<PagingData<Post>> = userId.flatMapLatest {
        repository.loadUserWall(it, repository.getDb(), repository.getRK())
    }.cachedIn(viewModelScope)


    @OptIn(ExperimentalCoroutinesApi::class)
    val myData: Flow<PagingData<Post>> = appAuth.state
        .flatMapLatest { (myId, _) ->
        repository.loadMyWall(myId, repository.getDb(), repository.getMyRK())
    }.cachedIn(viewModelScope)
        .map {pagingData ->
            pagingData.map{
                it.copy(ownedByMe = true)
            }

        }

    private val _dataState = MutableLiveData<FeedModelState>(FeedModelState.Idle)

    private val _user = MutableStateFlow(typical)
    val user: Flow<User>
        get() = _user
    private val _userLastCompName = MutableStateFlow("job is not defined")
    val userLastCompName: Flow<String>
    get() = _userLastCompName

    init {
        loadPosts()
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

    fun takeUsersLastJob(userId: Long) = viewModelScope.launch {
            try {
                _dataState.value = FeedModelState.Refreshing
                _userLastCompName.value = repository.getUsersLastJob(userId).name
                _dataState.value = FeedModelState.Idle
            } catch (e: Exception) {
                _dataState.value = FeedModelState.Error
            }
        }
}