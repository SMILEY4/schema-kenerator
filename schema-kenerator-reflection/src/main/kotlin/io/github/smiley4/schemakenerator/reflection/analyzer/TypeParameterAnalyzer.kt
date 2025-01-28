package io.github.smiley4.schemakenerator.reflection.analyzer

import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.core.typedata.TypeParameterData
import io.github.smiley4.schemakenerator.core.typedata.WrappedTypeId
import io.github.smiley4.schemakenerator.core.typedata.find
import io.github.smiley4.schemakenerator.core.typedata.findOrThrow
import io.github.smiley4.schemakenerator.core.typedata.toWrappedTypeId
import kotlin.reflect.KClass
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection

/**
 * Analysis functions for type parameters using reflection.
 */
class TypeParameterAnalyzer {

    /**
     * Analyzes all type parameters of the type from the given context and adds new types to the given context
     * @param context the context with the current type to analyze,
     * @return the resulting list of [TypeParameterData]
     */
    fun analyzeTypeParameters(context: ReflectionTypeAnalyzerModule.Context): List<TypeParameterData> {
        val classProvidesNames = context.clazz.typeParameters.size == context.type.arguments.size
        return context.type.arguments.indices.map { index ->
            val name = if (classProvidesNames) context.clazz.typeParameters[index].name else "T"
            val argType = context.type.arguments[index]
            val resolvedType = resolveTypeProjection(argType, context)
            TypeParameterData(
                name = name,
                type = resolvedType.id,
                nullable = (argType.type?.isMarkedNullable ?: false) || resolvedType.nullable,
            )
        }
    }


    /**
     * Resolves the given [KTypeProjection] to a [TypeData] with nullability information.
     * @param typeProjection the type projection to resolve
     * @param context the context with the current type and information to analyze,
     * @return the resolved [TypeData] with nullability information
     */
    fun resolveTypeProjection(typeProjection: KTypeProjection, context: ReflectionTypeAnalyzerModule.Context): WrappedTypeId {
        if (typeProjection.type == null) {
            return WrappedTypeId(
                id = resolveWildcard(context.knownTypeData).id,
                nullable = false
            )
        }
        return when (val classifier = typeProjection.type?.classifier) {
            is KClass<*> -> context.analyze(typeProjection.type!!, classifier).toWrappedTypeId()
            is KTypeParameter -> {
                val typeParameter = context.knownTypeParameters.findOrThrow(classifier.name)
                WrappedTypeId(
                    id = context.knownTypeData.findOrThrow(typeParameter.type).id,
                    nullable = typeParameter.nullable
                )
            }
            else -> throw IllegalArgumentException("Unhandled classifier type: '$classifier'.")
        }
    }


    /**
     * Resolves a wildcard type. Either creating a new one and adding it to the known types of returning an already existing wildcard type.
     */
    private fun resolveWildcard(knownTypeData: MutableList<TypeData>): TypeData {
        val wildcard = TypeData.createWildcard()
        return knownTypeData.find(wildcard.id) ?: wildcard.also { knownTypeData.add(it) }
    }

}
