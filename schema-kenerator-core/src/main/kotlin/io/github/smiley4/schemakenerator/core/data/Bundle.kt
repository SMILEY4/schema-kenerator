package io.github.smiley4.schemakenerator.core.data

import kotlin.reflect.KType

/**
 * Data bundled together with other related data
 */
data class Bundle<T>(
    val data: T,
    val supporting: List<T>
)

fun <T> Bundle<T>.flatten(): List<T> = listOf(data) + supporting


fun Bundle<BaseTypeData>.flattenToMap(): Map<TypeId, BaseTypeData> = flatten().associateBy { it.id }

fun <T,R> Bundle<T>.map(transform: (T) -> R): Bundle<R> {
    return Bundle(
        data = transform(data),
        supporting = supporting.map(transform),
    )
}

fun Bundle<KType>.mapToInputType(): Bundle<InputType> {
    return this.map { KTypeInput(it) }
}