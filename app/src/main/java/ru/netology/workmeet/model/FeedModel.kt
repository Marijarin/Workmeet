package ru.netology.workmeet.model

import ru.netology.workmeet.dto.FeedItem


data class FeedModel(
    val posts: List<FeedItem> = emptyList(),
    val empty: Boolean = false,
    )

sealed interface FeedModelState{
    object Error: FeedModelState
    object Refreshing: FeedModelState
    object Loading: FeedModelState
    object Idle: FeedModelState
}
