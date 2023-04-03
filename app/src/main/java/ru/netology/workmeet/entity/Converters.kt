package ru.netology.workmeet.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import ru.netology.workmeet.dto.UserPreview


class Converters {

    @TypeConverter
    fun listToJson(value: List<Long>?) = Gson().toJson(value)
    @TypeConverter
    fun usersListToJson(value: List<UserPreview>?) = Gson().toJson(value)
    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<Long>::class.java).toList()
    @TypeConverter
    fun jsonToUsersList(value: String) = Gson().fromJson(value, Array<UserPreview>::class.java).toList()

}
