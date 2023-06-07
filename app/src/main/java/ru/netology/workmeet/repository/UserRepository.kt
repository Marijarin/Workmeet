package ru.netology.workmeet.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.workmeet.entity.UserEntity

interface UserRepository {
    val data: Flow<List<UserEntity>>
    suspend fun getAllU()
}