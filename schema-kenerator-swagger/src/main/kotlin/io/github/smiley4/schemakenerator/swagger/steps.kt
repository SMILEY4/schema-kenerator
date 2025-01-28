package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.generator.DefaultSwaggerSchemaGenerationModule
import io.github.smiley4.schemakenerator.swagger.generator.SwaggerSchemaGenerationModule
import io.github.smiley4.schemakenerator.swagger.generator.SwaggerSchemaGeneratorImpl
import io.swagger.v3.oas.models.media.Schema

enum class OptionalHandling {
    REQUIRED,
    NON_REQUIRED
}

class SwaggerSchemaGenerationStepConfig {
    /**
     * How to handle optional parameters
     *
     * Example:
     * ```
     * class MyExample(val someValue: String = "hello")
     * ```
     * - with `optionalHandling = REQUIRED` => "someValue" is required (because is not nullable)
     * - with `optionalHandling = NON_REQUIRED` => "someValue" is not required (because a default value is provided)
     */
    var optionalHandling: OptionalHandling = OptionalHandling.REQUIRED

    val customModules = mutableListOf<SwaggerSchemaGenerationModule>()

    internal fun buildCustomModules(): List<SwaggerSchemaGenerationModule> {
        val allModules = listOf(
            DefaultSwaggerSchemaGenerationModule(
                optionalAsNonRequired = optionalHandling == OptionalHandling.NON_REQUIRED
            )
        ) + customModules
        return allModules.reversed()
    }

    fun custom(module: SwaggerSchemaGenerationModule) {
        customModules.add(module)
    }

    // todo: dsl for "custom"

}


/**
 * See [SwaggerSchemaGenerationStep]
 */
fun Bundle<TypeData>.generateSwaggerSchema(configBlock: SwaggerSchemaGenerationStepConfig.() -> Unit = {}): Bundle<SwaggerSchema> {
    val config = SwaggerSchemaGenerationStepConfig().apply(configBlock)
    return SwaggerSchemaGeneratorImpl(config.buildCustomModules()).process(this)
}


/**
 * See [SwaggerSchemaTitleStep]
 */
@Deprecated("Was renamed", ReplaceWith("withTitle"))
fun Bundle<SwaggerSchema>.withAutoTitle(type: TitleType = TitleType.FULL): Bundle<SwaggerSchema> {
    return this.withTitle(type)
}


/**
 * See [SwaggerSchemaTitleStep]
 */
fun Bundle<SwaggerSchema>.withTitle(type: TitleType = TitleType.FULL): Bundle<SwaggerSchema> {
    return withTitle(
        when (type) {
            TitleType.FULL -> TitleBuilder.BUILDER_FULL
            TitleType.SIMPLE -> TitleBuilder.BUILDER_SIMPLE
            TitleType.OPENAPI_FULL -> TitleBuilder.BUILDER_OPENAPI_FULL
            TitleType.OPENAPI_SIMPLE -> TitleBuilder.BUILDER_OPENAPI_SIMPLE
        }
    )
}


/**
 * See [SwaggerSchemaTitleStep]
 */
fun Bundle<SwaggerSchema>.withTitle(builder: (type: TypeData, types: Map<TypeId, TypeData>) -> String): Bundle<SwaggerSchema> {
    return SwaggerSchemaTitleStep(builder).process(this)
}


/**
 * See [SwaggerSchemaCoreAnnotationDefaultStep], [SwaggerSchemaCoreAnnotationDeprecatedStep], [SwaggerSchemaCoreAnnotationDescriptionStep],
 * [SwaggerSchemaCoreAnnotationExamplesStep], [SwaggerSchemaCoreAnnotationTitleStep], [SwaggerSchemaCoreAnnotationOptionalAndRequiredStep],
 * [SwaggerSchemaCoreAnnotationFormatStep], [SwaggerSchemaCoreAnnotationTypeStep]
 */
fun Bundle<SwaggerSchema>.handleCoreAnnotations(): Bundle<SwaggerSchema> {
    return this
        .let { SwaggerSchemaCoreAnnotationOptionalAndRequiredStep().process(this) }
        .let { SwaggerSchemaCoreAnnotationDefaultStep().process(this) }
        .let { SwaggerSchemaCoreAnnotationDeprecatedStep().process(this) }
        .let { SwaggerSchemaCoreAnnotationDescriptionStep().process(this) }
        .let { SwaggerSchemaCoreAnnotationExamplesStep().process(this) }
        .let { SwaggerSchemaCoreAnnotationTitleStep().process(this) }
        .let { SwaggerSchemaCoreAnnotationFormatStep().process(this) }
        .let { SwaggerSchemaCoreAnnotationTypeStep().process(this) }
}


/**
 * See [SwaggerSchemaAnnotationStep], [SwaggerArraySchemaAnnotationStep]
 */
fun Bundle<SwaggerSchema>.handleSchemaAnnotations(): Bundle<SwaggerSchema> {
    return this
        .let { SwaggerSchemaAnnotationStep().process(this) }
        .let { SwaggerArraySchemaAnnotationStep().process(this) }
}


/**
 * See [SwaggerMergePropertyAttributesStep]
 */
fun Bundle<SwaggerSchema>.mergePropertyAttributesIntoType(): Bundle<SwaggerSchema> {
    return SwaggerMergePropertyAttributesStep().process(this)
}


/**
 * See [SwaggerSchemaCompileInlineStep]
 */
fun Bundle<SwaggerSchema>.compileInlining(): CompiledSwaggerSchema {
    return SwaggerSchemaCompileInlineStep().compile(this)
}


/**
 * See [SwaggerSchemaCompileReferenceStep]
 */
fun Bundle<SwaggerSchema>.compileReferencing(pathType: RefType = RefType.OPENAPI_FULL): CompiledSwaggerSchema {
    return compileReferencing(
        when (pathType) {
            RefType.FULL -> TitleBuilder.BUILDER_FULL
            RefType.SIMPLE -> TitleBuilder.BUILDER_SIMPLE
            RefType.OPENAPI_FULL -> TitleBuilder.BUILDER_OPENAPI_FULL
            RefType.OPENAPI_SIMPLE -> TitleBuilder.BUILDER_OPENAPI_SIMPLE
        }
    )
}


/**
 * See [SwaggerSchemaCompileReferenceStep]
 */
fun Bundle<SwaggerSchema>.compileReferencing(
    builder: (type: TypeData, types: Map<TypeId, TypeData>) -> String
): CompiledSwaggerSchema {
    return SwaggerSchemaCompileReferenceStep(builder).compile(this)
}


/**
 * See [SwaggerSchemaCompileReferenceRootStep]
 */
fun Bundle<SwaggerSchema>.compileReferencingRoot(pathType: RefType = RefType.OPENAPI_FULL): CompiledSwaggerSchema {
    return compileReferencingRoot(
        when (pathType) {
            RefType.FULL -> TitleBuilder.BUILDER_FULL
            RefType.SIMPLE -> TitleBuilder.BUILDER_SIMPLE
            RefType.OPENAPI_FULL -> TitleBuilder.BUILDER_OPENAPI_FULL
            RefType.OPENAPI_SIMPLE -> TitleBuilder.BUILDER_OPENAPI_SIMPLE
        }
    )
}


/**
 * See [SwaggerSchemaCompileReferenceRootStep]
 */
fun Bundle<SwaggerSchema>.compileReferencingRoot(
    builder: (type: TypeData, types: Map<TypeId, TypeData>) -> String
): CompiledSwaggerSchema {
    return SwaggerSchemaCompileReferenceRootStep(builder).compile(this)
}


/**
 * See [SwaggerSchemaCustomizeStep.customizeTypes]
 */
fun Bundle<SwaggerSchema>.customizeTypes(
    action: (typeData: TypeData, typeSchema: Schema<*>) -> Unit
): Bundle<SwaggerSchema> {
    return SwaggerSchemaCustomizeStep().customizeTypes(this, action)
}


/**
 * See [SwaggerSchemaCustomizeStep.customizeProperties]
 */
fun Bundle<SwaggerSchema>.customizeProperties(
    action: (propertyData: MemberData, propertySchema: Schema<*>) -> Unit
): Bundle<SwaggerSchema> {
    return SwaggerSchemaCustomizeStep().customizeProperties(this, action)
}
