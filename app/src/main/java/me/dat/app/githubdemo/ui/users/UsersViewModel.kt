package me.dat.app.githubdemo.ui.users

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.dat.app.githubdemo.data.repository.GitHubRepository
import me.dat.app.githubdemo.entities.UserEntity
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val repository: GitHubRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState(isLoading = true))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _event = Channel<Event>(Channel.CONFLATED)
    val event: SharedFlow<Event> =
        _event.receiveAsFlow().shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    init {
        fetchUsers()
    }

    private fun fetchUsers(since: Int? = null) {
        viewModelScope.launch {
            if (since == null) _uiState.update { it.copy(isLoading = true) }
            try {
                val pageResult = repository.getUsers(since)
                val userList = if (since == null) {
                    pageResult.items
                } else {
                    _uiState.value.users + pageResult.items
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        users = userList.toImmutableList(),
                        nextToken = if (pageResult.hasMore) NextToken.Fetch(userList.lastOrNull()?.id) else NextToken.None,
                    )
                }
            } catch (e: Throwable) {
                _event.send(Event.Error(e))
                Timber.d(e)
            }
        }
    }

    fun loadMore() {
        when (val next = _uiState.value.nextToken) {
            is NextToken.Fetch -> {
                fetchUsers(next.since)
            }

            is NextToken.Search -> {
                searchUsers(next.query, next.page)
            }

            NextToken.None -> {
                // do nothing
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        // TODO realtime search
//        if (query.isBlank()) {
//            fetchUsers()
//        } else {
//            searchUsers(query)
//        }
    }

    fun onSearchQuerySubmitted(query: String) {
        if (query.isBlank()) {
            fetchUsers()
        } else {
            searchUsers(query)
        }
    }

    private fun searchUsers(query: String, page: Int = 1) {
        viewModelScope.launch {
            if (page == 1) _uiState.update { it.copy(isLoading = true) }
            try {
                val pageResult = repository.searchUsers(query, page)
                val userList = if (page == 1) {
                    pageResult.items
                } else {
                    _uiState.value.users + pageResult.items
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        users = userList.toImmutableList(),
                        nextToken = if (pageResult.hasMore) NextToken.Search(query, page + 1) else NextToken.None,
                    )
                }
            } catch (e: Exception) {
                _event.send(Event.Error(e))
                Timber.d(e)
            }
        }
    }

    data class UiState(
        val users: ImmutableList<UserEntity> = persistentListOf(),
        val nextToken: NextToken = NextToken.None,
        val isLoading: Boolean = false,
        val error: String? = null
    ) {
        val hasMore: Boolean
            get() = nextToken !is NextToken.None
    }

    sealed class NextToken {
        data class Search(val query: String, val page: Int) : NextToken()
        data class Fetch(val since: Int?) : NextToken()
        data object None : NextToken()
    }

    sealed class Event {
        data class Error(val value: Throwable) : Event()
    }
}
