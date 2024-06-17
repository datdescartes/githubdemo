package me.dat.app.githubdemo.entities

data class Page<T>(
    val items: List<T>,
    val hasMore: Boolean
)