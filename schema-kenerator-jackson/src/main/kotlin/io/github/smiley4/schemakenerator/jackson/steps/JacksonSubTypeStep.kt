package io.github.smiley4.schemakenerator.jackson.steps

import com.fasterxml.jackson.annotation.JsonSubTypes
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

/**
 * Finds all additional subtypes from jackson [JsonSubTypes]-Annotation and adds them to the list
 * - input: [KType]
 * - output: [KType] including additional subtypes
 */
class JacksonSubTypeStep(
    private val maxRecursionDepth: Int = 10,
    val typeProcessing: (types: Collection<KType>) -> Collection<BaseTypeData>
) {

    fun process(data: Collection<KType>): List<KType> {
        var depth = 0
        var countPrev = 0
        var entries = data.toList()

        do {
            countPrev = entries.size
            entries = entries
                .let { typeProcessing(it) }
                .flatMap { findSubTypes(it) }
                .distinct()
            depth++
        } while (countPrev != entries.size && depth < maxRecursionDepth)

        return entries
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