package ru.netology.workmeet.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.workmeet.dto.User
import ru.netology.workmeet.model.FeedModelState
import ru.netology.workmeet.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
  val repository: UserRepository
): ViewModel() {

  init{
    loadUsers()
  }

  private val _dataState = MutableLiveData<FeedModelState>(FeedModelState.Idle)
  val dataState: LiveData<FeedModelState>
    get() = _dataState

  val data: Flow<List<User>> =  repository.data
    .map { users ->
      users.map{
          jobEntity ->
        jobEntity.toDto()
      }
    }.flowOn(Dispatchers.Default)

  private fun loadUsers(){
    viewModelScope.launch{
      try {
        _dataState.value = FeedModelState.Loading
        repository.getAllU()
        _dataState.value = FeedModelState.Idle
      }catch (e: Exception) {
        _dataState.value = FeedModelState.Error
      }
    }
  }

}