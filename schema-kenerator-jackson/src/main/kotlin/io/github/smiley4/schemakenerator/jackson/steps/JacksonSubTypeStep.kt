package io.github.smiley4.schemakenerator.jackson.steps

import com.fasterxml.jackson.annotation.JsonSubTypes
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.flatten
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

/**
 * Finds additional subtypes from jackson [JsonSubTypes]-annotation.
 * An additional step to add missing subtype-supertype relations later may be required - see [io.github.smiley4.schemakenerator.core.steps.ConnectSubTypesStep].
 * @param maxRecursionDepth how many "levels" to search for subtypes
 * @param typeProcessing processor to get annotation data from [KType]
 */
class JacksonSubTypeStep(
    private val maxRecursionDepth: Int = 10,
    val typeProcessing: (type: KType) -> Bundle<BaseTypeData> // todo: change to "Ktype -> AnnotationData[]" and add steps: "Reflection#processAnnotations(KType)" and "Core#collectAnnotations(Bundle<BaseTypeData>)",
) {

    /**
     * Finds additional subtypes from jackson [JsonSubTypes]-annotation.
     */
    fun process(data: KType): Bundle<KType> {
        var depth = 0
        var countPrev = 0
        var subtypes = listOf(data)

        do {
            countPrev = subtypes.size

            val foundSubtypes = subtypes
                .let { process(it) }
                .flatMap { findSubTypes(it) }

            subtypes = (subtypes + foundSubtypes)
                .distinct()
                .toMutableList()

            depth++
        } while (countPrev != subtypes.size && depth < maxRecursionDepth)

        return Bundle(
            data = data,
            supporting = subtypes.toMutableList().also { it.remove(data) }
        )
    }

    private fun process(types: List<KType>): Collection<BaseTypeData> {
        return types
            .map { typeProcessing(it) }
            .flatMap { it.flatten() }
    }

    private fun findSubTypes(typeData: BaseTypeData): List<KType> {
        @Suppress("UNCHECKED_CAST")
        return typeData.annotations
            .find { it.name == JsonSubTypes::class.qualifiedName!! }
            ?.let { it.values["value"] as Array<JsonSubTypes.Type> }
            ?.let { it.map { v -> v.value } }
            ?.let { it.map { v -> v.starProjectedType } }
            ?: emptyList()
    }

}