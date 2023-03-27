package ru.netology.workmeet.dto


sealed interface FeedItem {
    val id: Long
}

data class Post(
    override val id: Long,
    val authorId: Long = 0L,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val published: String,
    val coords: Coordinates?,
    val link: String?,
    val likeOwnerIds: List<Long> = emptyList(),
    val mentionIds: List<Long> = emptyList(),
    val mentionedMe: Boolean = false,
    val likedByMe: Boolean = false,
    val attachment: Attachment?,
    val ownedByMe: Boolean = false,
    val users:List<UserPreview>,

): FeedItem

data class Event(
    override val id: Long,
    val authorId: Long = 0L,
    val author: String,
    val authorAvatar: String,
    val authorJob: String,
    val content: String,
    val datetime: String,
    val published: String,
    val coords: Coordinates?,
    val type: EventType,
    val likeOwnerIds: List<Long> = emptyList(),
    val likedByMe: Boolean,
    val speakerIds: List<Long>,
    val participantsIds: List<Long>,
    val participatedByMe: Boolean = false,
    val attachment: Attachment?,
    val link: String,
    val ownedByMe: Boolean = false,
    val users: List<UserPreview>,
    ): FeedItem

enum class EventType{
    OFFLINE, ONLINE
}

data class Job(
    override val id: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    val link: String?,
): FeedItem

data class Ad(
    override val id: Long,
    val image: String,
): FeedItem

data class Separator(
    override val id: Long,
    val description: String
): FeedItem

data class User(
    val id: Long,
    val avatar: String,
    val login: String,
    val name: String,

)
data class Attachment(
    val url: String,
    val type: AttachmentType
)
enum class AttachmentType {
    IMAGE, VIDEO, AUDIO
}

data class Coordinates(
    val lat: String,
    val long: String,
)

data class UserPreview(
    val name: String,
    val avatar: String,
)

