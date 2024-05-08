package io.github.smiley4.schemakenerator.jackson.steps

import com.fasterxml.jackson.annotation.JsonSubTypes
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

/**
 * Finds all additional subtypes from jackson [JsonSubTypes]-Annotation and adds them to the list
 * - input: [KType]
 * - output: [KType] including additional subtypes
 */
class JacksonSubTypeStep(
    private val maxRecursionDepth: Int = 10,
    val typeProcessing: (type: KType) -> Bundle<BaseTypeData>
) {

    fun process(data: KType): Bundle<KType> {
        var depth = 0
        var countPrev = 0
        var subtypes = listOf(data)

        do {
            countPrev = subtypes.size
            subtypes = subtypes
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
            .map { typeProcessing(it) }
            .flatMap { listOf(it.data) + it.supporting }
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