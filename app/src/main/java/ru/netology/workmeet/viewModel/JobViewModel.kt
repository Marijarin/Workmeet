package ru.netology.workmeet.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.workmeet.auth.AppAuth
import ru.netology.workmeet.dto.Job
import ru.netology.workmeet.model.FeedModelState
import ru.netology.workmeet.repository.JobRepository
import ru.netology.workmeet.util.SingleLiveEvent
import javax.inject.Inject

private val empty = Job(
    id = 0,
    name ="company",
    position = "director",
    start = "then",
    finish ="now",
    link = null
)
@HiltViewModel
class JobViewModel @Inject constructor(
    private val repository: JobRepository,
    appAuth: AppAuth
): ViewModel() {
    val userId = MutableStateFlow(0L)
    fun setUserId(id: Long) {
        userId.run {
            this.value = id
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<List<Job>> = userId.flatMapLatest {
        repository.data
            .map { jobs ->
                jobs.filter {
                    it.userId == userId.value
                }
            }
    }.flowOn(Dispatchers.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    val myData: Flow<List<Job>> = appAuth.state.flatMapLatest {
        repository.data
            .map { jobs ->
                jobs.filter {
                    it.userId == appAuth.state.value.id
                }
            }
    }.flowOn(Dispatchers.Default)

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

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState.Refreshing
            repository.removeById(id)
            _dataState.value = FeedModelState.Idle
        } catch (e: Exception) {
            _dataState.value = FeedModelState.Error
        }
    }
    fun changeContent(name:String, position: String, start: String, finish: String, link: String){
        val nameJ = name.trim()
        val positionJ = position.trim()
        val startJ = start.trim()
        val finishJ = finish.trim()
        val linkJ = link.trim()
        if (edited.value?.name == nameJ
            && edited.value?.position == positionJ
            && edited.value?.start == startJ
            && edited.value?.finish == finishJ
            && edited.value?.link == linkJ
        ) {
            return
        }
        edited.value = edited.value?.copy(name = nameJ, position = positionJ, start = startJ, finish = finishJ, link = linkJ)
    }

}