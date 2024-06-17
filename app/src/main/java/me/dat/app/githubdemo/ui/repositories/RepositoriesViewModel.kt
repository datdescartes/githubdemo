package me.dat.app.githubdemo.ui.repositories

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
import me.dat.app.githubdemo.entities.RepositoryEntity
import me.dat.app.githubdemo.entities.UserDetailEntity
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RepositoriesViewModel @Inject constructor(
    private val repository: GitHubRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState(isLoading = true))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _event = Channel<Event>(Channel.CONFLATED)
    val event: SharedFlow<Event> =
        _event.receiveAsFlow().shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    private lateinit var username: String

    fun init(username: String) {
        this.username = username
        fetchUserDetail(username)
        fetchUserRepos(username)
    }

    private fun fetchUserDetail(username: String) {
        viewModelScope.launch {
            try {
                val user = repository.getUserDetail(username)
                _uiState.update {
                    it.copy(
                        user = user,
                    )
                }
            } catch (e: Throwable) {
                _event.send(Event.Error(e))
                Timber.d(e)
            }
        }
    }

    private fun fetchUserRepos(username: String, page: Int = 1) {
        viewModelScope.launch {
            try {
                val pageResult = repository.getUserRepos(username, page)
                val repoList = if (page == 1) {
                    pageResult.items
                } else {
                    _uiState.value.repositories + pageResult.items
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        repositories = repoList.toImmutableList(),
                        nextToken = if (pageResult.hasMore) NextToken(
                            username,
                            page + 1,
                        ) else null
                    )
                }
            } catch (e: Throwable) {
                _event.send(Event.Error(e))
                _uiState.update { it.copy(isLoading = false) }
                Timber.d(e)
            }
        }
    }

    fun loadMore() {
        _uiState.value.nextToken?.let {
            if (!_uiState.value.isLoading) fetchUserRepos(it.username, it.page)
        }
    }

    data class UiState(
        val repositories: ImmutableList<RepositoryEntity> = persistentListOf(),
        val nextToken: NextToken? = null,
        val user: UserDetailEntity = UserDetailEntity(
            avatarUrl = "",
            username = "",
            fullName = "",
            followers = 0,
            following = 0
        ),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    data class NextToken(
        val username: String,
        val page: Int,
    )

    sealed class Event {
        data class Error(val value: Throwable) : Event()
    }
}
