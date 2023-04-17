package ru.netology.workmeet.dto

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName


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
    val users:List<UserPreview> = emptyList(),

    ): FeedItem

data class Event(
    override val id: Long,
    val authorId: Long = 0L,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val datetime: String,
    val published: String,
    val coords: Coordinates?,
    val type: EventType,
    val likeOwnerIds: List<Long> = emptyList(),
    val likedByMe: Boolean = false,
    val speakerIds: List<Long> = emptyList(),
    val participantsIds: List<Long> = emptyList(),
    val participatedByMe: Boolean = false,
    val attachment: Attachment?,
    val link: String,
    val ownedByMe: Boolean = false,
    val users: List<UserPreview> = emptyList(),
    ): FeedItem

enum class EventType{
    OFFLINE, ONLINE, EMPTY
}

data class Job(
    override val id: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    val link: String?,

): FeedItem
data class User(
    val id: Long,
    val avatar: String?,
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
    @SerializedName("longi")
    val longi: String,
)

data class UserPreview(
    val id: Long,
    val name: String,
    val avatar: String?,
){
    fun toJson(userPreview: UserPreview): String = Gson().toJson(userPreview)
    companion object {
        fun fromJson(value: String) = Gson().fromJson(value, UserPreview::class.java)
    }

}

