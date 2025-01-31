package io.github.smiley4.schemakenerator.reflection.analyzer

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability

/**
 * Analysis functions for subtypes using reflection.
 */
class SubtypeAnalyzer {

    /**
     * Analyzes the subtypes of the class of the given context (e.g. all types extending / inheriting from the current type)
     * and adds new types to the given context.
     * @param context the context with the current type to analyze,
     * @return the list of subtypes as [TypeData]
     */
    fun analyzeSubtypes(context: ReflectionTypeAnalyzerModule.Context): List<TypeId> {
        return context.clazz.sealedSubclasses
            // "clazz.sealedSubclasses" contains all subclasses ignoring type parameters.
            // this filter step makes we only collect the actual subtypes where the type parameters also match
            .filter { subtype -> subtype.supertypes.any { supertype -> supertype == context.type.withNullability(false) } }
            .map { subtype -> context.analyze(subtype.starProjectedType, subtype).typeData.id }
    }

}
