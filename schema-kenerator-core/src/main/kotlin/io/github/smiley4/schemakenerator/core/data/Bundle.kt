package io.github.smiley4.schemakenerator.core.data

/**
 * Data bundled together with other related data
 */
data class Bundle<T>(
    val data: T,
    val supporting: List<T>
)

fun <T> Bundle<T>.flatten(): List<T> = listOf(data) + supporting


fun Bundle<BaseTypeData>.flattenToMap(): Map<TypeId, BaseTypeData> = flatten().associateBy { it.id }
