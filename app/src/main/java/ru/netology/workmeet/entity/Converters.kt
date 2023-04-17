package ru.netology.workmeet.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import ru.netology.workmeet.dto.AttachmentType
import ru.netology.workmeet.dto.UserPreview


class Converters {
    @TypeConverter
    fun fromAttachmentType(value: AttachmentType) = value.name
    @TypeConverter
    fun toAttachmentType(value: String) = enumValueOf<AttachmentType>(value)
    @TypeConverter
    fun listToJson(value: List<Long>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<Long>::class.java).toList()

    @TypeConverter
    fun fromUserListToJson(value: List<UserPreview>): String {
        val json = value.map { it.toJson(it) }
        return Gson().toJson(json)
    }
    @TypeConverter
    fun toUserListFromJson(value: String): List<UserPreview> {
        val jsonSet = Gson().fromJson(value, Array<String>::class.java)
        return jsonSet.map{
            UserPreview.fromJson(it)
        }
    }

}
