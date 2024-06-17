package me.dat.app.githubdemo.data.network

import me.dat.app.githubdemo.data.model.RepositoryDto
import me.dat.app.githubdemo.data.model.SearchUserResponseDto
import me.dat.app.githubdemo.data.model.UserDetailDto
import me.dat.app.githubdemo.data.model.UserDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApi {

    @GET("users")
    suspend fun getUsers(
        @Query("since") since: Int? = null,
        @Query("per_page") perPage: Int? = null, // default = 30
    ): List<UserDto>

    @GET("users/{username}")
    suspend fun getUserDetail(@Path("username") username: String): UserDetailDto

    @GET("users/{username}/repos")
    suspend fun getUserRepos(
        @Path("username") username: String,
        @Query("type") type: String? = null, // default = "owner",
        @Query("sort") sort: String? = null, // default = "full_name",
        @Query("direction") direction: String? = null, // default = "asc",
        @Query("per_page") perPage: Int? = null, // default = 30,
        @Query("page") page: Int? = null, // default = 1
    ): List<RepositoryDto>

    @GET("search/users")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("sort") sort: String? = null,
        @Query("order") order: String? = null, // default = "desc"
        @Query("per_page") perPage: Int? = null, // default = 30
        @Query("page") page: Int? = null, // default = 1
    ): SearchUserResponseDto
}
