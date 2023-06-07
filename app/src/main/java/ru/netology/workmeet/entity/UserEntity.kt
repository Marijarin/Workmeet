package ru.netology.workmeet.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.workmeet.dto.Post
import ru.netology.workmeet.dto.User
import ru.netology.workmeet.dto.UserPreview

@Entity
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id:Long,
    val avatar: String?,
    val login: String,
    val name: String,
){
    fun toDto() = User(
        id,
        avatar,
       login,
        name,
    )
    companion object{
        fun fromDto(dto: User) =
            UserEntity(
                dto.id,
                dto.avatar,
                dto.login,
                dto.name,

            )
    }
}
fun List<User>.toEntity(): List<UserEntity> = map(UserEntity::fromDto)
