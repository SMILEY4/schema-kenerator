package io.github.smiley4.schemakenerator.reflection.analyzer

import io.github.smiley4.schemakenerator.core.data.TypeName
import io.github.smiley4.schemakenerator.core.data.WrappedTypeData
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
        typeData = provider(context.id),
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
