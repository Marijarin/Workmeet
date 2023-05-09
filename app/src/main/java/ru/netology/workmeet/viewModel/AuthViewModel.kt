package ru.netology.workmeet.viewModel

import android.net.Uri
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.auth.AuthState
import ru.netology.workmeet.error.AppError
import ru.netology.workmeet.model.MediaModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
     private val appAuth: AppAuth
) : ViewModel() {
    val data: LiveData<AuthState> = appAuth.state
        .asLiveData(Dispatchers.Default)
    val authenticated: Boolean
        get() = appAuth.state.value.id != 0L

    private val noPhoto = MediaModel(null, null)

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<MediaModel>
        get() = _photo

    fun updateUser(login: String, password: String) = viewModelScope.launch {
        try {
            appAuth.updateUser(login, password)
        } catch (e: Exception) {
            println(e)
            throw AppError.from(e)
        }
    }

     fun registerUser(login: String, password: String, name: String, file: File?) = viewModelScope.launch {
        try {
            appAuth.registerUser(login, password, name, file)
        } catch (e: Exception) {
            println(e)
            throw AppError.from(e)
        }
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = MediaModel(uri, file)
    }

}