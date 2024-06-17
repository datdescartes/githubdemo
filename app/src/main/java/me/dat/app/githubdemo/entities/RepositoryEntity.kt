package me.dat.app.githubdemo.entities

data class RepositoryEntity(
    val id: Int,
    val name: String,
    val fullName: String,
    val ownerAvatarUrl: String,
    val ownerLogin: String,
    val htmlUrl: String,
    val description: String? = null,
    val language: String? = null,
    val stargazersCount: Int
)
