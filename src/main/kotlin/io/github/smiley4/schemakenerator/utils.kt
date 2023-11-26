package io.github.smiley4.schemakenerator

import kotlin.reflect.KType
import kotlin.reflect.typeOf

inline fun <reified T> getKType(): KType {
    return typeOf<T>()
}


 inline fun <T, K, V> Iterable<T>.associateIndexed(transform: (index: Int, T) -> Pair<K, V>): Map<K, V> {
    return this.mapIndexed(transform).associate { it }
}