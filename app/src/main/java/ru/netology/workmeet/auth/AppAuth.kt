package ru.netology.workmeet.auth

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.ViewModelLifecycle
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import ru.netology.workmeet.api.ApiService
import ru.netology.workmeet.error.ApiError
import ru.netology.workmeet.error.NetworkError
import ru.netology.workmeet.error.WhoKnowsError
import java.io.File
import javax.inject.Inject
import javax.inject.Scope
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context) {

    private val TOKEN_KEY = "TOKEN_KEY"
    private val ID_KEY = "ID_KEY"
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _state: MutableStateFlow<AuthState>
    init {
        val id = prefs.getLong(ID_KEY, 0)
        val token = prefs.getString(TOKEN_KEY, null)

        if (id == 0L || token == null) {
            _state = MutableStateFlow(AuthState())
            with(prefs.edit()) {
                clear()
                apply()
            }
        } else {
            _state = MutableStateFlow(AuthState(id, token))
        }

    }

    val state = _state.asStateFlow()



    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint{
        fun getApiService(): ApiService
    }

    @Synchronized
    fun setAuth(id: Long, token: String) {
        _state.value = AuthState(id, token)
        with(prefs.edit()) {
            putLong(ID_KEY, id)
            putString(TOKEN_KEY, token)
            apply()
        }

    }

    @Synchronized
    fun removeAuth() {
        _state.value = AuthState()
        with(prefs.edit()) {
            clear()
            apply()
        }
    }

    suspend fun updateUser(login: String, password: String) {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
            val response = entryPoint.getApiService().updateUser(login, password)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val newAuth = response.body() ?: throw ApiError(response.code(), response.message())
            newAuth.token?.let { setAuth(newAuth.id, it) }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw  WhoKnowsError
        }
    }

    suspend fun registerUser(login: String, password: String, name: String, file: File?) {
        try {
            var fileData: MultipartBody.Part? = null
            if (file!=null) {
                fileData = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    file.asRequestBody()
                )
            }
            val entryPoint = EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
            val response = entryPoint.getApiService().registerUser(
                login.toRequestBody(),
                password.toRequestBody(),
                name.toRequestBody(),
                fileData
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val newAuth = response.body() ?: throw ApiError(response.code(), response.message())
            newAuth.token?.let { setAuth(newAuth.id, it) }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw  WhoKnowsError
        }
    }

}