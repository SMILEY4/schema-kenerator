package io.github.smiley4.schemakenerator.reflection.analyze.provided

import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.core.typedata.TypeId
import io.github.smiley4.schemakenerator.reflection.analyze.ReflectionTypeAnalyzerModule
import kotlin.reflect.full.starProjectedType

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
        return context.clazz.sealedSubclasses.map { context.analyze(it.starProjectedType, it).typeData.id }
    }

}
