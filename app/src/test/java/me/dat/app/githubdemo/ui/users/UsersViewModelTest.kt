package me.dat.app.githubdemo.ui.users

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import me.dat.app.githubdemo.data.repository.GitHubRepository
import me.dat.app.githubdemo.entities.Page
import me.dat.app.githubdemo.entities.UserEntity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@OptIn(ExperimentalCoroutinesApi::class)
class UsersViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: GitHubRepository
    private lateinit var viewModel: UsersViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = UsersViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test fetchUsers success`() = runTest {
        // Arrange
        val userEntities = listOf(
            UserEntity(name = "User1", username = "user1", id = 1, avatarUrl = "url1"),
            UserEntity(name = "User2", username = "user2", id = 2, avatarUrl = "url2")
        )
        val page = Page(items = userEntities, hasMore = false)
        coEvery { repository.getUsers(null) } returns page

        // Act
        viewModel.fetchUsers()

        // Assert
        val uiState = viewModel.uiState.value
        assert(uiState.users == userEntities.toImmutableList())
        assert(!uiState.isLoading)
        assert(uiState.nextToken is UsersViewModel.NextToken.None)
    }

    @Test
    fun `test fetchUsers error`() = runTest {
        // Arrange
        val error = RuntimeException("Test exception")
        coEvery { repository.getUsers(null) } throws error

        // Act
        viewModel.fetchUsers()

        // Assert
        val uiState = viewModel.uiState.value
        assert(uiState.users.isEmpty())
        assert(!uiState.isLoading)  // This should now pass
        assert(uiState.nextToken is UsersViewModel.NextToken.None)
    }

    @Test
    fun `test onSearchQueryChanged`() = runTest {
        // Arrange
        val query = "octocat"
        val userEntities = listOf(
            UserEntity(name = "User1", username = "user1", id = 1, avatarUrl = "url1")
        )
        val page = Page(items = userEntities, hasMore = false)
        coEvery { repository.searchUsers(query, 1) } returns page

        // Act
        viewModel.onSearchQueryChanged(query)

        // Assert
        advanceTimeBy(500)
        val uiState = viewModel.uiState.value
        assert(uiState.users == userEntities.toImmutableList())
        assert(!uiState.isLoading)
        assert(uiState.nextToken is UsersViewModel.NextToken.None)
    }

    @Test
    fun `test loadMore fetchUsers`() = runTest {
        // Arrange
        val initialUserEntities = listOf(
            UserEntity(name = "User1", username = "user1", id = 1, avatarUrl = "url1")
        )
        val additionalUserEntities = listOf(
            UserEntity(name = "User2", username = "user2", id = 2, avatarUrl = "url2")
        )
        val initialPage = Page(items = initialUserEntities, hasMore = true)
        val additionalPage = Page(items = additionalUserEntities, hasMore = false)
        coEvery { repository.getUsers(null) } returns initialPage
        coEvery { repository.getUsers(1) } returns additionalPage

        // Act
        viewModel.fetchUsers()
        viewModel.loadMore()

        // Assert
        val uiState = viewModel.uiState.value
        val expectedUserList = (initialUserEntities + additionalUserEntities).toImmutableList()
        assert(uiState.users == expectedUserList) {
            "Expected: $expectedUserList, Actual: ${uiState.users}"
        }
        assert(!uiState.isLoading)
        assert(uiState.nextToken is UsersViewModel.NextToken.None)
    }

    @Test
    fun `test loadMore searchUsers`() = runTest {
        // Arrange
        val query = "octocat"
        val initialUserEntities = listOf(
            UserEntity(name = "User1", username = "user1", id = 1, avatarUrl = "url1")
        )
        val additionalUserEntities = listOf(
            UserEntity(name = "User2", username = "user2", id = 2, avatarUrl = "url2")
        )
        val initialPage = Page(items = initialUserEntities, hasMore = true)
        val additionalPage = Page(items = additionalUserEntities, hasMore = false)
        coEvery { repository.searchUsers(query, 1) } returns initialPage
        coEvery { repository.searchUsers(query, 2) } returns additionalPage

        // Act
        viewModel.onSearchQueryChanged(query)
        advanceTimeBy(500)
        viewModel.loadMore()

        // Assert
        val uiState = viewModel.uiState.value
        val expectedUserList = (initialUserEntities + additionalUserEntities).toImmutableList()
        assert(uiState.users == expectedUserList) {
            "Expected: $expectedUserList, Actual: ${uiState.users}"
        }
        assert(!uiState.isLoading)
        assert(uiState.nextToken is UsersViewModel.NextToken.None)
    }
}
