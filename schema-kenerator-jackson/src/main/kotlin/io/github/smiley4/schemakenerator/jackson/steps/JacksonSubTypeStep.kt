package io.github.smiley4.schemakenerator.jackson.steps

import com.fasterxml.jackson.annotation.JsonSubTypes
import old.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.InputType
import io.github.smiley4.schemakenerator.core.data.KTypeInput
import io.github.smiley4.schemakenerator.core.data.flatten
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

/**
 * Finds additional subtypes from jackson [JsonSubTypes]-annotation.
 * An additional step to add missing subtype-supertype relations
 * later may be required - see [io.github.smiley4.schemakenerator.core.steps.AddMissingSubtypeSupertypeRelations].
 * @param maxRecursionDepth how many "levels" to search for subtypes
 * @param typeProcessing processor to get annotation data from [KType]
 */
class JacksonSubTypeStep(
    private val maxRecursionDepth: Int = 10,
    val typeProcessing: (type: KType) -> Bundle<BaseTypeData>
) {

    /**
     * Finds additional subtypes from jackson [JsonSubTypes]-annotation.
     */
    fun process(data: InputType): Bundle<InputType> {
        var depth = 0
        var countPrev = 0
        var subtypes = listOf(data)

        do {
            countPrev = subtypes.size

            val foundSubtypes = subtypes
                .let { process(it) }
                .flatMap { findSubTypes(it) }

            subtypes = (subtypes + foundSubtypes)
                .distinctBy {
                    when(it) {
                        is KTypeInput -> it.kType
                        else -> throw IllegalArgumentException("Unsupported input type: '$it'")
                    }
                }
                .toMutableList()

            depth++
        } while (countPrev != subtypes.size && depth < maxRecursionDepth)

        return Bundle(
            data = data,
            supporting = subtypes.toMutableList().also { it.remove(data) }
        )
    }

    private fun process(types: List<InputType>): Collection<BaseTypeData> {
        return types
            .map {
                when(it) {
                    is KTypeInput -> typeProcessing(it.kType)
                    else -> throw IllegalArgumentException("Unsupported input type: '$it'")
                }
            }
            .flatMap { it.flatten() }
    }

    private fun findSubTypes(typeData: BaseTypeData): List<InputType> {
        @Suppress("UNCHECKED_CAST")
        return typeData.annotations
            .find { it.name == JsonSubTypes::class.qualifiedName!! }
            ?.let { it.values["value"] as Array<JsonSubTypes.Type> }
            ?.let { it.map { v -> v.value } }
            ?.let { it.map { v -> v.starProjectedType } }
            ?.let { it.map { v -> KTypeInput(v) } }
            ?: emptyList()
    }

}
