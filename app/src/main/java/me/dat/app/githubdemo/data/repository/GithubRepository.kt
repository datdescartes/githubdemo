package me.dat.app.githubdemo.data.repository

import me.dat.app.githubdemo.data.model.mapper.toEntity
import me.dat.app.githubdemo.data.model.mapper.toEntityList
import me.dat.app.githubdemo.data.network.GitHubApi
import me.dat.app.githubdemo.entities.Page
import me.dat.app.githubdemo.entities.RepositoryEntity
import me.dat.app.githubdemo.entities.UserDetailEntity
import me.dat.app.githubdemo.entities.UserEntity
import javax.inject.Inject

private const val PER_PAGE = 30

class GitHubRepository @Inject constructor(
    private val api: GitHubApi
) {
    suspend fun getUsers(since: Int? = null): Page<UserEntity> {
        val userDtos = api.getUsers(since, perPage = PER_PAGE)
        val items = userDtos.toEntityList()
        val hasMore = items.isNotEmpty() && items.size == PER_PAGE
        return Page(items, hasMore)
    }

    suspend fun getUserDetail(username: String): UserDetailEntity {
        val userDetailDto = api.getUserDetail(username)
        return userDetailDto.toEntity()
    }

    suspend fun getUserRepos(username: String, page: Int? = 1): Page<RepositoryEntity> {
        val repoDtos = api.getUserRepos(username, perPage = PER_PAGE, page = page)
        val items = repoDtos.toEntityList()
        val hasMore = items.isNotEmpty() && items.size == PER_PAGE
        return Page(items, hasMore)
    }

    suspend fun searchUsers(
        query: String,
        page: Int? = 1,
    ): Page<UserEntity> {
        val searchResult = api.searchUsers(query, perPage = PER_PAGE, page = page)
        val items = searchResult.items.toEntityList()
        val hasMore = items.isNotEmpty() && items.size == PER_PAGE
        return Page(items, hasMore)
    }
}
