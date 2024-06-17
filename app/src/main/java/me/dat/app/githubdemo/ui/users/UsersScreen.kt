package me.dat.app.githubdemo.ui.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.collectLatest
import me.dat.app.githubdemo.R
import me.dat.app.githubdemo.entities.UserEntity
import me.dat.app.githubdemo.ui.theme.GithubDemoTheme

@Composable
fun UsersScreen(
    viewModel: UsersViewModel = hiltViewModel(), onUserClick: (UserEntity) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val res = LocalContext.current.resources
    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is UsersViewModel.Event.Error -> {
                    snackBarHostState.showSnackbar(
                        message = res.getString(R.string.error_message)
                    )
                }
            }
        }
    }

    UiStateHandler(
        uiState = uiState,
        snackBarHostState = snackBarHostState,
        onUserClick = onUserClick,
        onQueryChanged = viewModel::onSearchQueryChanged,
        onSearchSubmit = viewModel::onSearchQuerySubmitted,
        onLoadmore = viewModel::loadMore,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UiStateHandler(
    uiState: UsersViewModel.UiState,
    snackBarHostState: SnackbarHostState,
    onUserClick: (UserEntity) -> Unit,
    onQueryChanged: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onLoadmore: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    Scaffold(snackbarHost = { SnackbarHost(snackBarHostState) }, topBar = {
        TopAppBar(title = {
            SearchBar(query = searchQuery, onQueryChanged = { query ->
                searchQuery = query
                isSubmitted = false
                onQueryChanged(query)
            }, onSearch = {
                isSubmitted = true
                onSearchSubmit(searchQuery)
            })
        })
    }) {
        Column(modifier = Modifier.padding(it)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            LazyColumn(state = listState) {
                items(uiState.users) { user ->
                    UserItem(user = user, onClick = onUserClick)
                }
                if (uiState.hasMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
        LaunchedEffect(listState) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull() }
                .collect { lastVisibleItem ->
                    if (lastVisibleItem != null && lastVisibleItem.index == uiState.users.size - 1) {
                        onLoadmore()
                    }
                }
        }
    }
}

@Composable
private fun SearchBar(
    query: String, onQueryChanged: (String) -> Unit, onSearch: () -> Unit
) {
    var text by remember { mutableStateOf(query) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        BasicTextField(
            value = text,
            onValueChange = {
                text = it
                onQueryChanged(it)
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = {
                onSearch()
            }),
            singleLine = true,
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search, contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (text.isEmpty()) {
                            Text(
                                text = "Search...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                }
            },
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UsersScreenPreview() {
    GithubDemoTheme {
        UiStateHandler(
            uiState = UsersViewModel.UiState(
                users = persistentListOf(
                    UserEntity(
                        name = "Octocat",
                        username = "octocat",
                        id = 1,
                        avatarUrl = "https://github.com/images/abc.png"
                    ),

                    UserEntity(
                        name = "user2",
                        username = "user2",
                        id = 1,
                        avatarUrl = "https://github.com/images/abc.png"
                    )
                ),
                isLoading = true,
            ),
            snackBarHostState = remember { SnackbarHostState() },
            onUserClick = { },
            onQueryChanged = { },
            onSearchSubmit = { },
            onLoadmore = { },
        )
    }
}
