package ru.netology.workmeet.entity

import android.util.JsonReader
import android.util.JsonWriter
import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.workmeet.dto.*
import java.io.StringReader
import java.io.StringWriter


@Entity
data class PostEntity(
    @PrimaryKey
    val id: Long,
    val authorId: Long = 0L,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val published: String,
    @Embedded
    val coords: CoordsEmbeddable?,
    val link: String?,
    @Ignore
    val likeOwnerIds: List<Long> = emptyList(),
    val mentionIds: List<Long> = emptyList(),
    val mentionedMe: Boolean = false,
    val likedByMe: Boolean = false,
    @Embedded
    val attachment: AttachmentEmbeddable?,
    val ownedByMe: Boolean = false,
    @Ignore
    val users: List<UserPreview>,
){
    fun toDto() = Post(
        id,
        authorId,
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
                dto.authorId,
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
@Entity(primaryKeys = ["id, userId"])
data class PostUserPreviewCrossRef(
    val id: Long,
    val userId: Long
)
data class PostWithUserPreviews(
    @Embedded val post: Post,
    @Relation (
        parentColumn = "id",
        entityColumn = "userId",
        associateBy = Junction(PostUserPreviewCrossRef::class)
            )
    val users: List<UserPreview>
)

data class AttachmentEmbeddable (
    var url: String,
    var type: AttachmentType,
) {
    fun toDto () = Attachment (url, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}
data class CoordsEmbeddable (
    val lat: String,
    val long: String,
) {
    fun toDto () = Coordinates (lat, long)

    companion object {
        fun fromDto(dto: Coordinates?) = dto?.let {
            CoordsEmbeddable(it.lat, it.long)
        }
    }
}
class Converters {

    @TypeConverter
    fun listToJson(value: List<Long>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<Long>::class.java).toList()
}


