package me.dat.app.githubdemo.ui.repositories

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.dat.app.githubdemo.R
import me.dat.app.githubdemo.entities.RepositoryEntity
import me.dat.app.githubdemo.entities.UserDetailEntity
import me.dat.app.githubdemo.ui.theme.GithubDemoTheme

@Composable
fun RepositoriesScreen(
    username: String,
    modifier: Modifier = Modifier,
    viewModel: RepositoriesViewModel = hiltViewModel(),
    onRepositoryClick: (RepositoryEntity) -> Unit = {},
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    val res = LocalContext.current.resources
    LaunchedEffect(Unit) {
        viewModel.init(username)
        viewModel.event.collectLatest { event ->
            when (event) {
                is RepositoriesViewModel.Event.Error -> {
                    snackBarHostState.showSnackbar(
                        message = res.getString(R.string.error_message)
                    )
                }
            }
        }
    }

    UiStateHandler(
        uiState = uiState,
        onRepositoryClick = onRepositoryClick,
        onLoadingMore = viewModel::loadMore,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UiStateHandler(
    uiState: RepositoriesViewModel.UiState,
    onRepositoryClick: (RepositoryEntity) -> Unit,
    onLoadingMore: () -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("@${uiState.user.username}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = SnackbarHostState()) }
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                val firstVisibleItemIndex =
                    remember { derivedStateOf { listState.firstVisibleItemIndex } }

                val avatarSize by animateDpAsState(
                    targetValue = if (firstVisibleItemIndex.value > 0) 40.dp else 100.dp,
                    label = "",
                )
                val textSize by animateFloatAsState(
                    targetValue = if (firstVisibleItemIndex.value > 0) 18f else 24f,
                    label = "",
                )
                val padding by animateDpAsState(
                    targetValue = if (firstVisibleItemIndex.value > 0) 8.dp else 16.dp,
                    label = "",
                )

                UserDetailItem(
                    user = uiState.user,
                    avatarSize = avatarSize,
                    textSize = textSize.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(padding)
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.repositories) { repo ->
                        RepositoryItem(repo, onClick = onRepositoryClick)
                    }
                    if (uiState.isLoading) {
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

                LaunchedEffect(listState) {
                    snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull() }
                        .collect { lastVisibleItem ->
                            if (lastVisibleItem != null && lastVisibleItem.index == uiState.repositories.size - 1) {
                                coroutineScope.launch {
                                    onLoadingMore()
                                }
                            }
                        }
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    GithubDemoTheme {
        UiStateHandler(
            uiState = RepositoriesViewModel.UiState(
                repositories = persistentListOf(
                    RepositoryEntity(
                        id = 1,
                        name = "Hello-World",
                        description = "This is your first repository",
                        language = "Kotlin",
                        stargazersCount = 42,
                        fullName = "octocat/Hello-World",
                        ownerAvatarUrl = "https://github.com/images/error/octocat_happy.gif",
                        ownerLogin = "octocat",
                        htmlUrl = "https://github.com/octocat/Hello-World",
                    )
                ),
                nextToken = null,
                user = UserDetailEntity(
                    avatarUrl = "https://github.com/images/error/octocat_happy.gif",
                    username = "octocat",
                    fullName = "Monalisa Octocat",
                    followers = 450,
                    following = 3581
                ),
                isLoading = false,
                error = null,
            ),
            onRepositoryClick = {},
            onLoadingMore = {},
            onBack = {}
        )
    }
}
