package io.github.smiley4.schemakenerator.jackson

import com.fasterxml.jackson.annotation.JsonSubTypes
import io.github.smiley4.schemakenerator.core.data.InitialKTypeData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

internal class JacksonSubTypeStep(
    private val maxRecursionDepth: Int = 10,
    private val typeProcessing: (type: InitialKTypeData) -> TypeDataGroup
) {

    fun process(input: InitialKTypeData): InitialKTypeData {
        var depth = 0
        var countPrev = 0
        var subtypes = input.types.toList()

        do {
            countPrev = subtypes.size

            val foundSubtypes = process(subtypes)
                .distinct()
                .flatMap { findSubTypes(it) }

            subtypes = (subtypes + foundSubtypes).distinct()

            depth++
        } while (countPrev != subtypes.size && depth < maxRecursionDepth)

        return InitialKTypeData(
            type = input.type,
            associatedTypes = subtypes.toMutableList().also { it.remove(input.type) }
        )
    }

    private fun process(types: List<KType>): List<TypeData> {
        return types
            .map { typeProcessing(InitialKTypeData(it, emptyList())) }
            .flatMap { it.typeData }
    }

    private fun findSubTypes(typeData: TypeData): List<KType> {
        @Suppress("UNCHECKED_CAST")
        return typeData.annotations
            .find { it.name == JsonSubTypes::class.qualifiedName!! }
            ?.let { it.values["value"] as Array<JsonSubTypes.Type> }
            ?.let { it.map { v -> v.value } }
            ?.let { it.map { v -> v.starProjectedType } }
            ?: emptyList()
    }

}
