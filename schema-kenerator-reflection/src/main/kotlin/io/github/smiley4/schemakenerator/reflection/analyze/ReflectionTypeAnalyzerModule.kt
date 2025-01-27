package io.github.smiley4.schemakenerator.reflection.analyze

import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.core.typedata.TypeId
import io.github.smiley4.schemakenerator.core.typedata.TypeParameterData
import io.github.smiley4.schemakenerator.core.typedata.WrappedTypeData
import io.github.smiley4.schemakenerator.reflection.data.MinimalTypeData
import kotlin.reflect.KClass
import kotlin.reflect.KType

interface ReflectionTypeAnalyzerModule {

    class Context(
        private val analyzer: ReflectionTypeAnalyzer,
        val id: TypeId,
        val type: KType,
        val clazz: KClass<*>,
        val knownTypeParameters: List<TypeParameterData>,
        val knownTypeData: MutableList<TypeData>
    ) {
        fun analyze(type: KType, clazz: KClass<*>): WrappedTypeData  {
            return this.analyzer.analyze(
                type = type,
                clazz = clazz,
                knownTypeParameters = knownTypeParameters,
                knownTypeData = knownTypeData
            )
        }
    }

    fun matches(): Boolean

    fun preAnalyze(context: Context): MinimalTypeData

    fun analyze(context: Context, minimalTypeData: MinimalTypeData): WrappedTypeData

}

