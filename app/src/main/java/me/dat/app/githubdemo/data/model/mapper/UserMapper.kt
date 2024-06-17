package me.dat.app.githubdemo.data.model.mapper

import me.dat.app.githubdemo.data.model.UserDto
import me.dat.app.githubdemo.entities.UserEntity

fun UserDto.toEntity(): UserEntity {
    return UserEntity(
        name = this.name,
        username = this.login,
        id = this.id,
        avatarUrl = this.avatarUrl
    )
}

fun List<UserDto>.toEntityList(): List<UserEntity> {
    return this.map { it.toEntity() }
}
