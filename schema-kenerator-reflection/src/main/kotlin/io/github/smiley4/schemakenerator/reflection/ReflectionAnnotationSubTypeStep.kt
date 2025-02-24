package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.data.InitialKTypeData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.reflection.ReflectionSteps.analyzeTypeUsingReflection
import io.github.smiley4.schemakenerator.reflection.data.SubType
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType


internal class ReflectionAnnotationSubTypeStep(private val maxRecursionDepth: Int = 10) {

    fun process(input: InitialKTypeData): InitialKTypeData {

        var depth = 0
        var countPrev: Int
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

    private fun process(types: List<KType>): Collection<TypeData> {
        return types
            .map { InitialKTypeData(it, emptyList()).analyzeTypeUsingReflection() }
            .flatMap { it.typeData }
    }


    private fun findSubTypes(data: TypeData): List<KType> {
        return data.annotations
            .filter { it.name == SubType::class.qualifiedName }
            .map { it.values["type"] as Class<*> }
            .map { it.kotlin.starProjectedType }
    }

}
