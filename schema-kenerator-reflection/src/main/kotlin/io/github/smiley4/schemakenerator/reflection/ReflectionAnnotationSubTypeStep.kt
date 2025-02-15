package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.InputType
import io.github.smiley4.schemakenerator.core.data.KTypeInput
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.reflection.data.SubType
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType


internal class ReflectionAnnotationSubTypeStep(private val maxRecursionDepth: Int = 10) {

    fun process(input: InputType): Bundle<InputType> {

        var depth = 0
        var countPrev: Int
        val subtypes: MutableList<InputType> = mutableListOf(input)

        do {
            countPrev = subtypes.size
            subtypes += process(subtypes)
                .flatMap { findSubTypes(it) }
                .distinct()
                .map { KTypeInput(it) }
            depth++
        } while (countPrev != subtypes.size && depth < maxRecursionDepth)

        return Bundle(
            data = input,
            supporting = subtypes
        )
    }

    private fun process(types: List<InputType>): Collection<TypeData> {
        return types
            .map { it.analyzeTypeUsingReflection() }
            .flatMap { listOf(it.data) + it.supporting }
    }


    private fun findSubTypes(data: TypeData): List<KType> {
        return data.annotations
            .filter { it.name == SubType::class.qualifiedName }
            .map { it.values["type"] as Class<*> }
            .map { it.kotlin.starProjectedType }
    }

}
