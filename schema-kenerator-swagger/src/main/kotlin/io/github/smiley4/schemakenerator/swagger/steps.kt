package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerArraySchemaAnnotationStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationTypeHintStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileInlineStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileReferenceRootStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileReferenceStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDefaultStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDeprecatedStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDescriptionStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationExamplesStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationOptionalAndRequiredStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationTitleStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCustomizeStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaGenerationStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaTitleStep
import io.github.smiley4.schemakenerator.swagger.steps.TitleBuilder
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


    /**
     * Whether to include the discriminator property from the marker annotation e.g. [io.github.smiley4.schemakenerator.core.steps.AbstractAddDiscriminatorStep.MARKER_ANNOTATION_NAME]
     */
    var discriminatorFromMarkerAnnotation: Boolean = true
}


/**
 * See [SwaggerSchemaGenerationStep]
 */
fun Bundle<BaseTypeData>.generateSwaggerSchema(configBlock: SwaggerSchemaGenerationStepConfig.() -> Unit = {}): Bundle<SwaggerSchema> {
    val config = SwaggerSchemaGenerationStepConfig().apply(configBlock)
    return SwaggerSchemaGenerationStep(
        optionalAsNonRequired = config.optionalHandling == OptionalHandling.NON_REQUIRED,
    ).generate(this)
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
fun Bundle<SwaggerSchema>.withTitle(builder: (type: BaseTypeData, types: Map<TypeId, BaseTypeData>) -> String): Bundle<SwaggerSchema> {
    return SwaggerSchemaTitleStep(builder).process(this)
}


/**
 * See [SwaggerSchemaCoreAnnotationDefaultStep], [SwaggerSchemaCoreAnnotationDeprecatedStep], [SwaggerSchemaCoreAnnotationDescriptionStep],
 * [SwaggerSchemaCoreAnnotationExamplesStep], [SwaggerSchemaCoreAnnotationTitleStep], [SwaggerSchemaCoreAnnotationOptionalAndRequiredStep]
 */
fun Bundle<SwaggerSchema>.handleCoreAnnotations(): Bundle<SwaggerSchema> {
    return this
        .let { SwaggerSchemaCoreAnnotationOptionalAndRequiredStep().process(this) }
        .let { SwaggerSchemaCoreAnnotationDefaultStep().process(this) }
        .let { SwaggerSchemaCoreAnnotationDeprecatedStep().process(this) }
        .let { SwaggerSchemaCoreAnnotationDescriptionStep().process(this) }
        .let { SwaggerSchemaCoreAnnotationExamplesStep().process(this) }
        .let { SwaggerSchemaCoreAnnotationTitleStep().process(this) }
}


/**
 * See [SwaggerSchemaAnnotationTypeHintStep]
 */
fun Bundle<SwaggerSchema>.handleSwaggerAnnotations(): Bundle<SwaggerSchema> {
    return SwaggerSchemaAnnotationTypeHintStep().process(this)
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
    builder: (type: BaseTypeData, types: Map<TypeId, BaseTypeData>) -> String
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
    builder: (type: BaseTypeData, types: Map<TypeId, BaseTypeData>) -> String
): CompiledSwaggerSchema {
    return SwaggerSchemaCompileReferenceRootStep(builder).compile(this)
}


/**
 * See [SwaggerSchemaCustomizeStep.customizeTypes]
 */
fun Bundle<SwaggerSchema>.customizeTypes(
    action: (typeData: BaseTypeData, typeSchema: Schema<*>) -> Unit
): Bundle<SwaggerSchema> {
    return SwaggerSchemaCustomizeStep().customizeTypes(this, action)
}


/**
 * See [SwaggerSchemaCustomizeStep.customizeProperties]
 */
fun Bundle<SwaggerSchema>.customizeProperties(
    action: (propertyData: PropertyData, propertySchema: Schema<*>) -> Unit
): Bundle<SwaggerSchema> {
    return SwaggerSchemaCustomizeStep().customizeProperties(this, action)
}
