package io.github.smiley4.schemakenerator.reflection.analyzer

import io.github.smiley4.schemakenerator.core.data.TypeData
import kotlin.reflect.KClass

/**
 * Analysis functions for supertypes using reflection.
 */
class SupertypeAnalyzer {

    /**
     * Analyzes the supertypes of the class of the given context (e.g. all types the given type extends / inherits from)
     * and adds new types to the given context.
     * @param context the context with the current type to analyze,
     * @return the list of supertypes as [TypeData]
     */
    fun analyzeSupertypes(context: ReflectionTypeAnalyzerModule.Context): List<TypeData> {
        return context.clazz.supertypes
            .filter { it.classifier != Any::class }
            .map { type ->
                when (val classifier = type.classifier) {
                    is KClass<*> -> context.analyze(type, classifier).typeData
                    else -> throw IllegalArgumentException("Unhandled classifier type")
                }
            }
    }

}
