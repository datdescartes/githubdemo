package me.dat.app.githubdemo.data.model.mapper

import me.dat.app.githubdemo.data.model.RepositoryDto
import me.dat.app.githubdemo.entities.RepositoryEntity

fun RepositoryDto.toEntity(): RepositoryEntity {
    return RepositoryEntity(
        id = id,
        name = name,
        fullName = fullName,
        ownerAvatarUrl = owner.avatarUrl,
        ownerLogin = owner.login,
        htmlUrl = htmlUrl,
        description = description,
        language = language,
        stargazersCount = stargazersCount
    )
}

fun List<RepositoryDto>.toEntityList(): List<RepositoryEntity> {
    return this.map { it.toEntity() }
}
