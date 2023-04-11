package ru.netology.workmeet.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.workmeet.dto.UserPreview

@Entity
data class UserPreviewEntity(
    @PrimaryKey(autoGenerate = true)
    val userId: Long,
    val name: String,
    val avatar: String,
) {
    fun toDto() = UserPreview(
        userId,
        name,
        avatar,
    )
    companion object {
        fun fromDto(dto: UserPreview) =
            UserPreviewEntity(
                dto.id,
                dto.name,
                dto.avatar,
            )
    }
}