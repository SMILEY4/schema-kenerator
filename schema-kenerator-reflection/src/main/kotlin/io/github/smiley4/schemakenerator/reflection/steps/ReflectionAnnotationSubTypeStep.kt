package io.github.smiley4.schemakenerator.reflection.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.reflection.data.SubType
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType


/**
 * Finds additional subtypes from [SubType]-annotation.
 * An additional step to add missing subtype-supertype relations
 * later may be required - see [io.github.smiley4.schemakenerator.core.steps.ConnectSubTypesStep].
 * @param maxRecursionDepth how many "levels" to search for subtypes
 */
class ReflectionAnnotationSubTypeStep(private val maxRecursionDepth: Int = 10) {

    fun process(data: KType): Bundle<KType> {

        var depth = 0
        var countPrev = 0
        val subtypes = mutableListOf(data)

        do {
            countPrev = subtypes.size
            subtypes += subtypes
                .let { process(it) }
                .flatMap { findSubTypes(it) }
                .distinct()
            depth++
        } while (countPrev != subtypes.size && depth < maxRecursionDepth)

        return Bundle(
            data = data,
            supporting = subtypes
        )
    }

    private fun process(types: List<KType>): Collection<BaseTypeData> {
        return types
            .map { ReflectionTypeProcessingStep().process(it) }
            .flatMap { listOf(it.data) + it.supporting }
    }


    private fun findSubTypes(data: BaseTypeData): List<KType> {
        return data.annotations
            .filter { it.name == SubType::class.qualifiedName }
            .map { it.values["type"] as Class<*> }
            .map { it.kotlin.starProjectedType }
    }

}
