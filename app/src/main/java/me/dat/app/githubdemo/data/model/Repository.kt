package me.dat.app.githubdemo.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepositoryDto(
    val id: Int,
    val name: String,
    @SerialName("full_name")
    val fullName: String,
    val owner: UserDto,
    @SerialName("html_url")
    val htmlUrl: String,
    val description: String? = null,
    val fork: Boolean,
    val language: String? = null,
    @SerialName("stargazers_count")
    val stargazersCount: Int
)
