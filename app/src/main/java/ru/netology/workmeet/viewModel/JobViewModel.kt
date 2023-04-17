package ru.netology.workmeet.viewModel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.dto.Job
import ru.netology.workmeet.model.FeedModelState
import ru.netology.workmeet.repository.JobRepository
import ru.netology.workmeet.util.SingleLiveEvent
import javax.inject.Inject
private val empty = Job(
    id = 0L,
    name ="",
    position = "",
    start = "",
    finish ="",
    link = null
)
@HiltViewModel
class JobViewModel @Inject constructor(
    private val repository: JobRepository,
    private val savedStateHandle: SavedStateHandle,
    appAuth: AppAuth
): ViewModel() {
    private val _data = MutableStateFlow(listOf<Job>())
    val data: Flow<List<Job>> = _data.asStateFlow()
    private val _dataState = MutableLiveData<FeedModelState>(FeedModelState.Idle)
    val dataState: LiveData<FeedModelState>
    get() = _dataState

    fun loadJobs(id: Long){
        viewModelScope.launch{
            try {
                _dataState.value = FeedModelState.Loading
                repository.getAllJ(id)
                _dataState.value = FeedModelState.Idle
            }catch (e: Exception) {
                _dataState.value = FeedModelState.Error
            }
        }
    }
    val edited = MutableLiveData(empty)
    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated

    fun save(id: Long) = viewModelScope.launch{
        edited.value?.let {
            _jobCreated.value = Unit
            viewModelScope.launch {
                try {
                    repository.save(it, id)
                    _dataState.value = FeedModelState.Idle
                } catch (e: Exception) {
                    _dataState.value = FeedModelState.Error
                }
            }
        }
        edited.value = empty
    }

    fun edit(job: Job) {
        edited.value = job
    }

    fun changeFinish(finish: String) {
        val finishDate = finish.trim()
        if (edited.value?.finish == finishDate) {
            return
        }
        edited.value = edited.value?.copy(finish = finishDate)
    }

}