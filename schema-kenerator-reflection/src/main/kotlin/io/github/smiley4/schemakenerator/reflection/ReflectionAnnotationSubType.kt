package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

class ReflectionAnnotationSubType(private val maxRecursionDepth: Int = 10) {

    fun process(data: Collection<KType>): List<KType> {

        var depth = 0
        var countPrev = 0
        var entries = data.toList()

        do {
            countPrev = entries.size
            entries = entries
                .let { ReflectionTypeProcessor().process(it) }
                .flatMap { findSubTypes(it) }
                .distinct()
            depth++
        } while (countPrev != entries.size && depth < maxRecursionDepth)

        return entries
    }

    private fun findSubTypes(data: BaseTypeData): List<KType> {
        return data.annotations
            .filter { it.name == SubType::class.qualifiedName }
            .map { it.values["type"] as Class<*> }
            .map { it.kotlin.starProjectedType }
    }

}