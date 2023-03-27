package ru.netology.workmeet.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserPreviewEntity(
    @PrimaryKey val userId:Long,
    val name: String,
    val avatar: String,
)