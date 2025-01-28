package io.github.smiley4.schemakenerator.reflection.analyzer

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.TypeParameterData
import io.github.smiley4.schemakenerator.core.data.WrappedTypeData
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

        fun analyze(type: KType, clazz: KClass<*>): WrappedTypeData {
            return this.analyzer.analyze(
                type = type,
                clazz = clazz,
                knownTypeParameters = knownTypeParameters,
                knownTypeData = knownTypeData
            )
        }

    }


    /**
     * @return whether this module applies to the given type.
     */
    fun applies(type: KType, clazz: KClass<*>): Boolean


    /**
     * A quick pre-analysis step with all input data in the given context.
     * @param context the input context with the current type to analyse
     * @return [MinimalTypeData] with some basic information
     */
    fun preAnalyze(context: Context): MinimalTypeData


    /**
     * The full type analysis.
     * @param context the input context with the current type to analyse and currently known additional data
     * @return [TypeData] with additional nullability information
     */
    fun analyze(context: Context, minimalTypeData: MinimalTypeData): WrappedTypeData

}

