package ru.netology.workmeet.viewModel

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
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.dto.*
import ru.netology.workmeet.model.PhotoModel
import ru.netology.workmeet.repository.PostRepository
import javax.inject.Inject

private val empty = Post(
    id = 0L,
    author = "",
    authorAvatar = "",
    authorJob = "",
    content = "",
    published = "",
    coords = null,
    link = "",
    attachment = null
)

private val noPhoto = PhotoModel(null, null)

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {
    private val cached = repository
        .data
        .cachedIn(viewModelScope)

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


}