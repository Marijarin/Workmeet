package ru.netology.workmeet.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.dto.Post
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
    link = "",
    attachment = null
)

private val noPhoto = PhotoModel(null, null)

@HiltViewModel
class WallViewModel @Inject constructor(
    private val repository: PostRepository,

    appAuth: AppAuth
) : ViewModel() {
    private val cached = repository
        .data
        .cachedIn(viewModelScope)
}