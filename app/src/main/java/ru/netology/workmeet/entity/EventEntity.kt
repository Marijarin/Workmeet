package ru.netology.workmeet.entity

import androidx.room.*
import ru.netology.workmeet.dto.*

@Entity
@TypeConverters(Converters::class)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long = 0L,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val datetime: String,
    val published: String,
    @Embedded
    var coords: CoordsEmbeddable?,
    val type: EventType,
    val likeOwnerIds: List<Long> = emptyList(),
    val likedByMe: Boolean,
    val speakerIds: List<Long>,
    val participantsIds: List<Long>,
    val participatedByMe: Boolean = false,
    @Embedded
    var attachment: AttachmentEmbeddable?,
    val link: String,
    val ownedByMe: Boolean = false,
    val users: Map<String, UserPreview>,
) {
    fun toDto() = Event(
        id,
        authorId = authorId,
        author,
        authorAvatar,
        authorJob,
        content,
        datetime,
        published,
        coords?.toDto(),
        type,
        likeOwnerIds,
        likedByMe,
        speakerIds,
        participantsIds,
        participatedByMe,
        attachment?.toDto(),
        link,
        ownedByMe,
        users,
    )

    companion object {
        fun fromDto(dto: Event) =
            EventEntity(
                dto.id,
                authorId = dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.authorJob,
                dto.content,
                dto.datetime,
                dto.published,
                CoordsEmbeddable.fromDto(dto.coords),
                dto.type,
                dto.likeOwnerIds,
                dto.likedByMe,
                dto.speakerIds,
                dto.participantsIds,
                dto.participatedByMe,
                AttachmentEmbeddable.fromDto(dto.attachment),
                dto.link,
                dto.ownedByMe,
                dto.users
            )
    }

}

fun List<Event>.toEntity(): List<EventEntity> = map(EventEntity::fromDto)