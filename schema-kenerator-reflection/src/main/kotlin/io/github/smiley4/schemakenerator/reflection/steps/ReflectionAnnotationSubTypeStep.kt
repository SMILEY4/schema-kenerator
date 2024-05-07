package io.github.smiley4.schemakenerator.reflection.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.reflection.data.SubType
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType


/**
 * Finds all additional subtypes from [SubType]-Annotation and adds them to the list
 * - input: [KType]
 * - output: [KType] including additional subtypes
 */
class ReflectionAnnotationSubTypeStep(private val maxRecursionDepth: Int = 10) {

    fun process(data: Collection<KType>): List<KType> {

        var depth = 0
        var countPrev = 0
        var entries = data.toList()

        do {
            countPrev = entries.size
            entries = entries
                .let { ReflectionTypeProcessingStep().process(it) }
                .flatMap { findSubTypes(it) }
                .distinct()
            depth++
        } while (countPrev != entries.size && depth < maxRecursionDepth)

        return (entries + data).distinct()
    }

    private fun findSubTypes(data: BaseTypeData): List<KType> {
        return data.annotations
            .filter { it.name == SubType::class.qualifiedName }
            .map { it.values["type"] as Class<*> }
            .map { it.kotlin.starProjectedType }
    }

}