package ru.netology.workmeet.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PostRemoteKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val id: Long
)

@Entity
data class EventRemoteKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val id: Long
)

@Entity
data class JobRemoteKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val id: Long
)

enum class KeyType {
    AFTER,
    BEFORE
}