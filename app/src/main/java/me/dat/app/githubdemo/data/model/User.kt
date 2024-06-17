package me.dat.app.githubdemo.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val name: String? = null,
    val login: String,
    val id: Int,
    @SerialName("avatar_url")
    val avatarUrl: String,
)
