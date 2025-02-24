package io.github.smiley4.schemakenerator.reflection.analyzer

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeParameterData
import io.github.smiley4.schemakenerator.core.data.WrappedTypeData
import kotlin.reflect.KClass
import kotlin.reflect.KType


/**
 * Analyzes the given type and returns the resulting [TypeData] (with additional nullability information)
 */
interface ReflectionTypeAnalyzer {

    fun analyze(
        type: KType,
        clazz: KClass<*>,
        knownTypeParameters: List<TypeParameterData>,
        knownTypeData: MutableList<TypeData>
    ): WrappedTypeData

}
