package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerArraySchemaAnnotationStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationTypeHintStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAutoTitleStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileInlineStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileReferenceRootStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileReferenceStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDefaultStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDeprecatedStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDescriptionStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationExamplesStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationTitleStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCustomizeStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaGenerationStep
import io.swagger.v3.oas.models.media.Schema

/**
 * See [SwaggerSchemaGenerationStep]
 */
fun Bundle<BaseTypeData>.generateSwaggerSchema(): Bundle<SwaggerSchema> {
    return SwaggerSchemaGenerationStep().generate(this)
}


/**
 * See [SwaggerSchemaAutoTitleStep]
 */
fun Bundle<SwaggerSchema>.withAutoTitle(type: TitleType = TitleType.FULL): Bundle<SwaggerSchema> {
    return SwaggerSchemaAutoTitleStep(type).process(this)
}


/**
 * See [SwaggerSchemaCoreAnnotationDefaultStep], [SwaggerSchemaCoreAnnotationDeprecatedStep], [SwaggerSchemaCoreAnnotationDescriptionStep],
 * [SwaggerSchemaCoreAnnotationExamplesStep], [SwaggerSchemaCoreAnnotationTitleStep]
 */
fun Bundle<SwaggerSchema>.handleCoreAnnotations(): Bundle<SwaggerSchema> {
    return this
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
fun Bundle<SwaggerSchema>.compileReferencing(pathType: RefType = RefType.FULL): CompiledSwaggerSchema {
    return SwaggerSchemaCompileReferenceStep(pathType).compile(this)
}


/**
 * See [SwaggerSchemaCompileReferenceRootStep]
 */
fun Bundle<SwaggerSchema>.compileReferencingRoot(pathType: RefType = RefType.FULL): CompiledSwaggerSchema {
    return SwaggerSchemaCompileReferenceRootStep(pathType).compile(this)
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
