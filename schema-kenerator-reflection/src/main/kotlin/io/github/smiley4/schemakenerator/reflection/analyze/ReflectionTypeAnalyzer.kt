package io.github.smiley4.schemakenerator.reflection.analyze

import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.core.typedata.TypeParameterData
import io.github.smiley4.schemakenerator.core.typedata.WrappedTypeData
import kotlin.reflect.KClass
import kotlin.reflect.KType


/**
 * Analyses the given type and returns the resulting [TypeData] (with additional nullability information)
 */
interface ReflectionTypeAnalyzer {

    fun analyze(type: KType, clazz: KClass<*>, knownTypeParameters: List<TypeParameterData>, knownTypeData: MutableList<TypeData>): WrappedTypeData

}