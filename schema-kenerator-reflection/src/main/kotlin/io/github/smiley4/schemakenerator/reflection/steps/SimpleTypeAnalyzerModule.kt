package io.github.smiley4.schemakenerator.reflection.steps

import io.github.smiley4.schemakenerator.core.typedata.TypeName
import io.github.smiley4.schemakenerator.core.typedata.WrappedTypeData
import io.github.smiley4.schemakenerator.reflection.analyzer.ReflectionTypeAnalyzerModule
import io.github.smiley4.schemakenerator.reflection.analyzer.TypeParameterAnalyzer
import io.github.smiley4.schemakenerator.reflection.data.MinimalTypeData
import kotlin.reflect.KClass
import kotlin.reflect.KType

class SimpleTypeAnalyzerModule(
    private val matcher: ReflectionTypeMatcher,
    private val provider: ReflectionCustomProvider
) : ReflectionTypeAnalyzerModule {

    private val typeParameterAnalyzer = TypeParameterAnalyzer()

    override fun applies(type: KType, clazz: KClass<*>) = matcher(type, clazz)

    override fun preAnalyze(context: ReflectionTypeAnalyzerModule.Context): MinimalTypeData {
        return MinimalTypeData(
            identifyingName = context.clazz.toTypeName(),
            descriptiveName = context.clazz.toTypeName(),
            typeParameters = typeParameterAnalyzer.analyzeTypeParameters(context)
        )
    }

    override fun analyze(context: ReflectionTypeAnalyzerModule.Context, minimalTypeData: MinimalTypeData) = WrappedTypeData(
        typeData = provider(),
        nullable = context.type.isMarkedNullable
    )


    /**
     * @return a [TypeName] for this class
     */
    private fun KClass<*>.toTypeName() = TypeName(
        full = this.qualifiedName ?: this.java.name,
        short = this.simpleName ?: this.java.name
    )

}