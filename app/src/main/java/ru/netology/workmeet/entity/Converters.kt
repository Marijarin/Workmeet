package ru.netology.workmeet.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import ru.netology.workmeet.dto.AttachmentType
import ru.netology.workmeet.dto.UserList
import ru.netology.workmeet.dto.UserPreview


class Converters {
    @TypeConverter
    fun userToJson(value: UserPreview): String = Gson().toJson(value)
    @TypeConverter
    fun fromAttachmentType(value: AttachmentType) = value.name

    @TypeConverter
    fun listToJson(value: List<Long>?) = Gson().toJson(value)

    @TypeConverter
    fun usersListToJson(value: UserList): String =
        Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<Long>::class.java).toList()

    @TypeConverter
    fun jsonToUsersList(value: String) =
        Gson().fromJson(value, UserList::class.java)

    @TypeConverter
    fun toAttachmentType(value: String) = enumValueOf<AttachmentType>(value)
    @TypeConverter
    fun jsonToUserPreview(value: String) = Gson().fromJson(value, UserPreview::class.java)

}
