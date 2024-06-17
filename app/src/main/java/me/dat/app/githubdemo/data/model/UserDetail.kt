package me.dat.app.githubdemo.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDetailDto(
    @SerialName("avatar_url")
    val avatarUrl: String,
    val login: String,
    val name: String? = null,
    val followers: Int,
    val following: Int
)
