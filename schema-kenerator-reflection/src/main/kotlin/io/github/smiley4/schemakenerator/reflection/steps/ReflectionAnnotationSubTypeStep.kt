package io.github.smiley4.schemakenerator.reflection.steps

import old.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.InputType
import io.github.smiley4.schemakenerator.core.data.KTypeInput
import io.github.smiley4.schemakenerator.reflection.data.SubType
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType


/**
 * Finds additional subtypes from [SubType]-annotation.
 * An additional step to add missing subtype-supertype relations
 * later may be required - see [io.github.smiley4.schemakenerator.core.steps.AddMissingSubtypeSupertypeRelations].
 * @param maxRecursionDepth how many "levels" to search for subtypes
 */
class ReflectionAnnotationSubTypeStep(private val maxRecursionDepth: Int = 10) {

    fun process(data: InputType): Bundle<InputType> {

        var depth = 0
        var countPrev: Int
        val subtypes: MutableList<InputType> = mutableListOf(data)

        do {
            countPrev = subtypes.size
            subtypes += process(subtypes)
                .flatMap { findSubTypes(it) }
                .distinct()
                .map { KTypeInput(it) }
            depth++
        } while (countPrev != subtypes.size && depth < maxRecursionDepth)

        return Bundle(
            data = data,
            supporting = subtypes
        )
    }

    private fun process(types: List<InputType>): Collection<BaseTypeData> {
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
