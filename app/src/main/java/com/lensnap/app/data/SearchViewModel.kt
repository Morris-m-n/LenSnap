package com.lensnap.app.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lensnap.app.models.EventSearchResult
import com.lensnap.app.models.UserSearchResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SearchViewModel(private val repository: SearchRepository) : ViewModel() {
    private val _userSearchResults = MutableStateFlow<List<UserSearchResult>>(emptyList())
    val userSearchResults: StateFlow<List<UserSearchResult>> get() = _userSearchResults

    private val _eventSearchResults = MutableStateFlow<List<EventSearchResult>>(emptyList())
    val eventSearchResults: StateFlow<List<EventSearchResult>> get() = _eventSearchResults

    fun performUserSearch(query: String) {
        viewModelScope.launch {
            val results = repository.searchUsers(query)
            _userSearchResults.value = results
        }
    }

    fun performEventSearch(query: String) {
        viewModelScope.launch {
            val results = repository.searchEvents(query)
            _eventSearchResults.value = results
        }
    }
}
