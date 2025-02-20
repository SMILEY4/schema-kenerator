//package io.github.smiley4.schemakenerator.core.data
//
//import io.github.smiley4.schemakenerator.core.InputType
//import kotlin.reflect.KType
//
///**
// * Data bundled together with other related data
// */
//data class Bundle<T>(
//    val data: T,
//    val supporting: List<T>
//)
//
//
///**
// * Flattens this bundle by merging [Bundle.data] and [Bundle.supporting] into a single list
// */
//fun <T> Bundle<T>.flatten(): List<T> = listOf(data) + supporting
//
//
///**
// * Flattens this bundle by merging [Bundle.data] and [Bundle.supporting] into a single map with the [TypeData.id] as keys.
// */
//fun Bundle<TypeData>.flattenToMap(): Map<TypeId, TypeData> = flatten().associateBy { it.id }
//
///**
// * Finds the type data with the given name
// */
//fun Bundle<TypeData>.find(id: TypeId) = flattenToMap()[id]
//
//
///**
// * Map all content of this bundle by applying the given transform.
// */
//fun <T, R> Bundle<T>.map(transform: (T) -> R): Bundle<R> {
//    return Bundle(
//        data = transform(data),
//        supporting = supporting.map(transform),
//    )
//}
//
//
///**
// * Map all content of this bundle from [KType] to [InputType] (i.e. [KTypeInput]).
// */
//fun Bundle<KType>.mapToInputType(): Bundle<InputType> {
//    return this.map { KTypeInput(it) }
//}
