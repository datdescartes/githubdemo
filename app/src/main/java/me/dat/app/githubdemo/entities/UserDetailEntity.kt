package me.dat.app.githubdemo.entities

data class UserDetailEntity(
    val avatarUrl: String,
    val username: String,
    val fullName: String?,
    val followers: Int,
    val following: Int
)
