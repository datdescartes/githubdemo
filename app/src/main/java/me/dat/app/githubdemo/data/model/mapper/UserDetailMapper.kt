package me.dat.app.githubdemo.data.model.mapper

import me.dat.app.githubdemo.data.model.UserDetailDto
import me.dat.app.githubdemo.entities.UserDetailEntity

fun UserDetailDto.toEntity(): UserDetailEntity {
    return UserDetailEntity(
        avatarUrl = avatarUrl,
        username = login,
        fullName = name,
        followers = followers,
        following = following
    )
}
