package ru.netology.workmeet.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import okio.IOException
import ru.netology.workmeet.api.ApiService
import ru.netology.workmeet.dao.UserDao
import ru.netology.workmeet.entity.UserEntity
import ru.netology.workmeet.entity.toEntity
import ru.netology.workmeet.error.NetworkError
import ru.netology.workmeet.error.WhoKnowsError
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao
    ): UserRepository {

    override val data: Flow<List<UserEntity>> = userDao.getAll()
    .flowOn(Dispatchers.Default)

    override suspend fun getAllU() {
        try {
            val response = apiService.getUsers()
            if (!response.isSuccessful) {
                throw Error(response.message())
            }
            val users = response.body() ?: throw Error(response.message())
            userDao.insert(users.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw WhoKnowsError
        }
    }

}