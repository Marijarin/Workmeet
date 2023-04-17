package ru.netology.workmeet.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.netology.workmeet.dto.Post
import ru.netology.workmeet.dto.UserPreview


@Entity
@TypeConverters(Converters::class)
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long = 0L,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val published: String,
    @Embedded
    var coords: CoordsEmbeddable?,
    val link: String?,
    val likeOwnerIds: List<Long> = emptyList(),
    val mentionIds: List<Long> = emptyList(),
    val mentionedMe: Boolean = false,
    val likedByMe: Boolean = false,
    @Embedded(prefix = "attachment_")
    var attachment: AttachmentEmbeddable?,
    val ownedByMe: Boolean = false,
    var users: List<UserPreview>,
) {
    fun toDto() = Post(
        id,
        authorId = authorId,
        author,
        authorAvatar,
        authorJob,
        content,
        published,
        coords?.toDto(),
        link,
        likeOwnerIds,
        mentionIds,
        mentionedMe,
        likedByMe,
        attachment?.toDto(),
        ownedByMe,
        users
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                authorId = dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.authorJob,
                dto.content,
                dto.published,
                CoordsEmbeddable.fromDto(dto.coords),
                dto.link,
                dto.likeOwnerIds,
                dto.mentionIds,
                dto.mentionedMe,
                dto.likedByMe,
                AttachmentEmbeddable.fromDto(dto.attachment),
                dto.ownedByMe,
                dto.users
            )
    }

}
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)




