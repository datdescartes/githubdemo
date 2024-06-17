package me.dat.app.githubdemo.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import me.dat.app.githubdemo.data.model.UserDto
import me.dat.app.githubdemo.data.model.RepositoryDto
import me.dat.app.githubdemo.data.model.SearchUserResponseDto
import me.dat.app.githubdemo.data.model.UserDetailDto
import me.dat.app.githubdemo.data.network.GitHubApi
import me.dat.app.githubdemo.entities.Page
import me.dat.app.githubdemo.entities.UserEntity
import me.dat.app.githubdemo.entities.RepositoryEntity
import me.dat.app.githubdemo.entities.UserDetailEntity
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class GitHubRepositoryTest {

    private lateinit var api: GitHubApi
    private lateinit var repository: GitHubRepository

    @Before
    fun setUp() {
        api = mockk()
        repository = GitHubRepository(api)
    }

    @Test
    fun `test getUsers`() = runTest {
        // Given
        val userDtos = listOf(
            UserDto(
                id = 1,
                login = "testuser",
                avatarUrl = "",
                name = null,
            )
        )
        coEvery { api.getUsers(null, 30) } returns userDtos

        // When
        val result: Page<UserEntity> = repository.getUsers()

        // Then
        coVerify { api.getUsers(null, 30) }
        assertEquals(1, result.items.size)
        assertEquals("testuser", result.items[0].username)
        assertEquals(false, result.hasMore)
    }

    @Test
    fun `test getUserDetail`() = runTest {
        // Given
        val username = "octocat"
        val userDetailDto = UserDetailDto(
            avatarUrl = "https://github.com/images/error/octocat_happy.gif",
            login = "octocat",
            name = "monalisa octocat",
            followers = 20,
            following = 0
        )
        val expected = UserDetailEntity(
            avatarUrl = "https://github.com/images/error/octocat_happy.gif",
            username = "octocat",
            fullName = "monalisa octocat",
            followers = 20,
            following = 0
        )

        coEvery { api.getUserDetail(username) } returns userDetailDto

        // When
        val result = repository.getUserDetail(username)

        // Then
        assertEquals(expected, result)
        coVerify { api.getUserDetail(username) }
    }

    @Test
    fun `test getUserRepos`() = runTest {
        // Given
        val repoDtos = listOf(
            RepositoryDto(
                id = 1,
                name = "testrepo",
                fullName = "",
                owner = UserDto(
                    name = null,
                    login = "Terrin",
                    id = 507,
                    avatarUrl = "Molli",
                ),
                htmlUrl = "Shardai",
                description = null,
                fork = true,
                stargazersCount = 3268,
            )
        )
        coEvery {
            api.getUserRepos(any(), any(), any(), any(), any(), any())
        } returns repoDtos

        // When
        val result: Page<RepositoryEntity> = repository.getUserRepos("testuser")

        // Then
        coVerify {
            api.getUserRepos("testuser", any(), any(), any(), any(), any())
        }
        assertEquals(1, result.items.size)
        assertEquals("testrepo", result.items[0].name)
        assertEquals(false, result.hasMore)
    }

    @Test
    fun `test searchUsers`() = runTest {
        // Given
        val searchUserResponseDto = SearchUserResponseDto(
            totalCount = 1, incompleteResults = false, items = listOf(
                UserDto(
                    id = 1,
                    login = "testuser",
                    avatarUrl = "",
                    name = null,
                )
            )
        )
        coEvery { api.searchUsers("test", perPage = 30, page = 1) } returns searchUserResponseDto

        // When
        val result: Page<UserEntity> = repository.searchUsers("test")

        // Then
        coVerify { api.searchUsers("test", perPage = 30, page = 1) }
        assertEquals(1, result.items.size)
        assertEquals("testuser", result.items[0].username)
        assertEquals(false, result.hasMore)
    }
}
