package ru.netology.workmeet.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.workmeet.dto.*


class Converters {

    @TypeConverter
    fun listToJson(value: List<Long>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<Long>::class.java).toList()

    @TypeConverter
    fun fromUserMapToJson(value: Map<String, UserPreview>)= Gson().toJson(value).toString()

    @TypeConverter
    fun toUserMapFromJson(value: String): Map<String, UserPreview> {
        val typeToken = object : TypeToken<Map<String, UserPreview>>() {}
         return Gson().fromJson(value, typeToken.type)
    }

}
