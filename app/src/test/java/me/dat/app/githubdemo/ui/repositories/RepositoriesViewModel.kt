package me.dat.app.githubdemo.ui.repositories

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import me.dat.app.githubdemo.data.repository.GitHubRepository
import me.dat.app.githubdemo.entities.Page
import me.dat.app.githubdemo.entities.RepositoryEntity
import me.dat.app.githubdemo.entities.UserDetailEntity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@OptIn(ExperimentalCoroutinesApi::class)
class RepositoriesViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: GitHubRepository
    private lateinit var viewModel: RepositoriesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = RepositoriesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test init fetches user detail and repositories`() = runTest {
        // Arrange
        val username = "octocat"
        val userDetail = UserDetailEntity(
            avatarUrl = "url",
            username = "octocat",
            fullName = "The Octocat",
            followers = 100,
            following = 50
        )
        val repositories = listOf(
            RepositoryEntity(
                id = 1,
                name = "Repo1",
                fullName = "octocat/Repo1",
                ownerAvatarUrl = "url",
                ownerLogin = "octocat",
                htmlUrl = "html_url",
                description = "description",
                language = "Kotlin",
                stargazersCount = 10
            )
        )
        val page = Page(items = repositories, hasMore = false)

        coEvery { repository.getUserDetail(username) } returns userDetail
        coEvery { repository.getUserRepos(username, 1) } returns page

        // Act
        viewModel.init(username)

        // Assert
        val uiState = viewModel.uiState.value
        assert(uiState.user == userDetail)
        assert(uiState.repositories == repositories.toImmutableList())
        assert(!uiState.isLoading)
        assert(uiState.nextToken == null)
    }

    @Test
    fun `test fetchUserDetail error`() = runTest {
        // Arrange
        val username = "octocat"
        val error = RuntimeException("Test exception")
        coEvery { repository.getUserDetail(username) } throws error

        // Act
        viewModel.init(username)

        // Assert
        coVerify { repository.getUserDetail(username) }
        val uiState = viewModel.uiState.value
        assert(uiState.user == UserDetailEntity("", "", "", 0, 0))
        assert(uiState.error == null)  // Error is sent as an event, not part of the state
    }

    @Test
    fun `test fetchUserRepos error`() = runTest {
        // Arrange
        val username = "octocat"
        val error = RuntimeException("Test exception")
        coEvery { repository.getUserRepos(username, 1) } throws error

        // Act
        viewModel.init(username)

        // Assert
        coVerify { repository.getUserRepos(username, 1) }
        val uiState = viewModel.uiState.value
        assert(uiState.repositories.isEmpty())
        assert(!uiState.isLoading)
    }

    @Test
    fun `test loadMore fetches additional repositories`() = runTest {
        // Arrange
        val username = "octocat"
        val initialRepositories = listOf(
            RepositoryEntity(
                id = 1,
                name = "Repo1",
                fullName = "octocat/Repo1",
                ownerAvatarUrl = "url",
                ownerLogin = "octocat",
                htmlUrl = "html_url",
                description = "description",
                language = "Kotlin",
                stargazersCount = 10
            )
        )
        val additionalRepositories = listOf(
            RepositoryEntity(
                id = 2,
                name = "Repo2",
                fullName = "octocat/Repo2",
                ownerAvatarUrl = "url",
                ownerLogin = "octocat",
                htmlUrl = "html_url",
                description = "description",
                language = "Kotlin",
                stargazersCount = 20
            )
        )
        val initialPage = Page(items = initialRepositories, hasMore = true)
        val additionalPage = Page(items = additionalRepositories, hasMore = false)

        coEvery { repository.getUserRepos(username, 1) } returns initialPage
        coEvery { repository.getUserRepos(username, 2) } returns additionalPage

        // Act
        viewModel.init(username)
        viewModel.loadMore()

        // Assert
        val uiState = viewModel.uiState.value
        val expectedRepositories = (initialRepositories + additionalRepositories).toImmutableList()
        assert(uiState.repositories == expectedRepositories)
        assert(!uiState.isLoading)
        assert(uiState.nextToken == null)
    }
}
