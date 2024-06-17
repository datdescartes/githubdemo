package me.dat.app.githubdemo.ui.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onErrorResume
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.dat.app.githubdemo.data.repository.GitHubRepository
import me.dat.app.githubdemo.entities.UserEntity
import org.jetbrains.annotations.VisibleForTesting
import timber.log.Timber
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class UsersViewModel @Inject constructor(
    private val repository: GitHubRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState(isLoading = true))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _event = Channel<Event>(Channel.CONFLATED)
    val event: SharedFlow<Event> =
        _event.receiveAsFlow().shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    private val queryChannel = Channel<String>()

    init {
        fetchUsers()
        viewModelScope.launch {
            queryChannel.receiveAsFlow().distinctUntilChanged().debounce(300L)
                .flatMapLatest { query ->
                    try {
                        val res = if (query.isEmpty()) {
                            val pageResult = repository.getUsers()
                            val nextToken = if (pageResult.hasMore) {
                                NextToken.Fetch(pageResult.items.lastOrNull()?.id)
                            } else NextToken.None
                            pageResult.items to nextToken
                        } else {
                            val pageResult = repository.searchUsers(query, 1)
                            val nextToken = if (pageResult.hasMore) {
                                NextToken.Search(query, 2)
                            } else NextToken.None
                            pageResult.items to nextToken
                        }
                        flowOf(res)
                    } catch (t: Throwable) {
                        Timber.d(t)
                        flowOf()
                    }
                }.collect { (items, nextToken) ->
                    _event.send(Event.ScrollToTop)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            users = items.toImmutableList(),
                            nextToken = nextToken,
                        )
                    }
                }
        }
    }

    @VisibleForTesting
    fun fetchUsers(since: Int? = null) {
        if (since == null) _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
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
                if (since == null) _event.send(Event.ScrollToTop)
            } catch (e: Throwable) {
                _uiState.update { it.copy(isLoading = false) }
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
        viewModelScope.launch {
            queryChannel.send(query.trim())
        }
    }

    fun onSearchQuerySubmitted(query: String) {
        if (query.isBlank()) {
            fetchUsers()
        } else {
            searchUsers(query)
        }
    }

    private fun searchUsers(query: String, page: Int = 1) {
        if (page == 1) _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
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
                        nextToken = if (pageResult.hasMore) NextToken.Search(
                            query,
                            page + 1
                        ) else NextToken.None,
                    )
                }
                if (page == 1) _event.send(Event.ScrollToTop)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
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
        data object ScrollToTop : Event()
    }
}
