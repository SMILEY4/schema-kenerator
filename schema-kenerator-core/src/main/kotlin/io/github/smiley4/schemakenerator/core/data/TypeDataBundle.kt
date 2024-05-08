package io.github.smiley4.schemakenerator.core.data

data class Bundle<T>(
    val data: T,
    val supporting: List<T>
)