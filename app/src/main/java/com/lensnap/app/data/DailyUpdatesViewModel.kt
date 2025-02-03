package com.lensnap.app.data

import android.net.Uri
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import com.lensnap.app.models.DailyUpdate

class DailyUpdatesViewModel(private val repository: DailyUpdateRepository, private val userViewModel: UserViewModel) : ViewModel() {
    private val _dailyUpdates = MutableLiveData<List<DailyUpdate>>()
    val dailyUpdates: LiveData<List<DailyUpdate>> get() = _dailyUpdates

    fun fetchDailyUpdates(userId: String) {
        userViewModel.fetchFollowing(userId) { followingUsers ->
            viewModelScope.launch {
                val updates = repository.getFollowingDailyUpdates(followingUsers.map { it.id })
                _dailyUpdates.value = updates
            }
        }
    }

    fun addDailyUpdate(userId: String, update: DailyUpdate, uri: Uri, username: String, profilePhotoUrl: String) {
        viewModelScope.launch {
            repository.addDailyUpdate(userId, update, uri, username, profilePhotoUrl)
            fetchDailyUpdates(userId)
        }
    }

    fun removeExpiredUpdates(userId: String) {
        viewModelScope.launch {
            repository.removeExpiredUpdates(userId)
            fetchDailyUpdates(userId)
        }
    }
}

class DailyUpdatesViewModelFactory(
    private val repository: DailyUpdateRepository,
    private val userViewModel: UserViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DailyUpdatesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DailyUpdatesViewModel(repository, userViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
