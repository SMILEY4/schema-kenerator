package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationTypeHintStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAutoTitleStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDefaultStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDeprecatedStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDescriptionStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationExamplesStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationTitleStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaGenerationStep

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
 * See [SwaggerSchemaCoreAnnotationDefaultStep]
 */
fun Bundle<SwaggerSchema>.handleDefaultAnnotation(): Bundle<SwaggerSchema> {
    return SwaggerSchemaCoreAnnotationDefaultStep().process(this)
}


/**
 * See [SwaggerSchemaCoreAnnotationDeprecatedStep]
 */
fun Bundle<SwaggerSchema>.handleDeprecatedAnnotation(): Bundle<SwaggerSchema> {
    return SwaggerSchemaCoreAnnotationDeprecatedStep().process(this)
}


/**
 * See [SwaggerSchemaCoreAnnotationDescriptionStep]
 */
fun Bundle<SwaggerSchema>.handleDescriptionAnnotation(): Bundle<SwaggerSchema> {
    return SwaggerSchemaCoreAnnotationDescriptionStep().process(this)
}


/**
 * See [SwaggerSchemaCoreAnnotationExamplesStep]
 */
fun Bundle<SwaggerSchema>.handleExampleAnnotation(): Bundle<SwaggerSchema> {
    return SwaggerSchemaCoreAnnotationExamplesStep().process(this)
}


/**
 * See [SwaggerSchemaCoreAnnotationTitleStep]
 */
fun Bundle<SwaggerSchema>.handleTitleAnnotation(): Bundle<SwaggerSchema> {
    return SwaggerSchemaCoreAnnotationTitleStep().process(this)
}


/**
 * See [SwaggerSchemaAnnotationTypeHintStep]
 */
fun Bundle<SwaggerSchema>.handleSwaggerTypeHintAnnotation(): Bundle<SwaggerSchema> {
    return SwaggerSchemaAnnotationTypeHintStep().process(this)
}


/**
 * See [SwaggerSchemaCompileStep]
 */
fun Bundle<SwaggerSchema>.compileInlining(): CompiledSwaggerSchema {
    return SwaggerSchemaCompileStep().compileInlining(this)
}


/**
 * See [SwaggerSchemaCompileStep]
 */
fun Bundle<SwaggerSchema>.compileReferencing(pathType: RefType = RefType.FULL): CompiledSwaggerSchema {
    return SwaggerSchemaCompileStep(pathType).compileReferencing(this)
}


/**
 * See [SwaggerSchemaCompileStep]
 */
fun Bundle<SwaggerSchema>.compileReferencingRoot(pathType: RefType = RefType.FULL): CompiledSwaggerSchema {
    return SwaggerSchemaCompileStep(pathType).compileReferencingRoot(this)
}
