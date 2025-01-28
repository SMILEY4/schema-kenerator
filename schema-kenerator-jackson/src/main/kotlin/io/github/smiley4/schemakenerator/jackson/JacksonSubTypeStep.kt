package io.github.smiley4.schemakenerator.jackson

import com.fasterxml.jackson.annotation.JsonSubTypes
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.InputType
import io.github.smiley4.schemakenerator.core.data.KTypeInput
import io.github.smiley4.schemakenerator.core.data.flatten
import io.github.smiley4.schemakenerator.core.data.TypeData
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

internal class JacksonSubTypeStep(private val maxRecursionDepth: Int = 10, private val typeProcessing: (type: KType) -> Bundle<TypeData>) {

    fun process(input: InputType): Bundle<InputType> {
        var depth = 0
        var countPrev = 0
        var subtypes = listOf(input)

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
            data = input,
            supporting = subtypes.toMutableList().also { it.remove(input) }
        )
    }

    private fun process(types: List<InputType>): Collection<TypeData> {
        return types
            .map {
                when(it) {
                    is KTypeInput -> typeProcessing(it.kType)
                    else -> throw IllegalArgumentException("Unsupported input type: '$it'")
                }
            }
            .flatMap { it.flatten() }
    }

    private fun findSubTypes(typeData: TypeData): List<InputType> {
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
