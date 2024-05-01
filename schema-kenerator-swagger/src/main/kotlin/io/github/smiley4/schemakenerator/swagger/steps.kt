package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.TitleType
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
fun Collection<BaseTypeData>.generateSwaggerSchema(): Collection<SwaggerSchema> {
    return SwaggerSchemaGenerationStep().generate(this)
}


/**
 * See [SwaggerSchemaAutoTitleStep]
 */
fun Collection<SwaggerSchema>.withAutoTitle(type: TitleType = TitleType.FULL): Collection<SwaggerSchema> {
    return SwaggerSchemaAutoTitleStep(type).process(this)
}


/**
 * See [SwaggerSchemaCoreAnnotationDefaultStep]
 */
fun Collection<SwaggerSchema>.withCoreAnnotationDefault(): Collection<SwaggerSchema> {
    return SwaggerSchemaCoreAnnotationDefaultStep().process(this)
}


/**
 * See [SwaggerSchemaCoreAnnotationDeprecatedStep]
 */
fun Collection<SwaggerSchema>.withCoreAnnotationDeprecated(): Collection<SwaggerSchema> {
    return SwaggerSchemaCoreAnnotationDeprecatedStep().process(this)
}


/**
 * See [SwaggerSchemaCoreAnnotationDescriptionStep]
 */
fun Collection<SwaggerSchema>.withCoreAnnotationDescription(): Collection<SwaggerSchema> {
    return SwaggerSchemaCoreAnnotationDescriptionStep().process(this)
}


/**
 * See [SwaggerSchemaCoreAnnotationExamplesStep]
 */
fun Collection<SwaggerSchema>.withCoreAnnotationExamples(): Collection<SwaggerSchema> {
    return SwaggerSchemaCoreAnnotationExamplesStep().process(this)
}


/**
 * See [SwaggerSchemaCoreAnnotationTitleStep]
 */
fun Collection<SwaggerSchema>.withCoreAnnotationTitle(): Collection<SwaggerSchema> {
    return SwaggerSchemaCoreAnnotationTitleStep().process(this)
}


/**
 * See [SwaggerSchemaCompileStep]
 */
fun Collection<SwaggerSchema>.compileInlining(pathType: TitleType = TitleType.FULL): List<CompiledSwaggerSchema> {
    return SwaggerSchemaCompileStep(pathType).compileInlining(this)
}


/**
 * See [SwaggerSchemaCompileStep]
 */
fun Collection<SwaggerSchema>.compileReferencing(pathType: TitleType = TitleType.FULL): List<CompiledSwaggerSchema> {
    return SwaggerSchemaCompileStep(pathType).compileReferencing(this)
}


/**
 * See [SwaggerSchemaCompileStep]
 */
fun Collection<SwaggerSchema>.compileReferencingRoot(pathType: TitleType = TitleType.FULL): List<CompiledSwaggerSchema> {
    return SwaggerSchemaCompileStep(pathType).compileReferencingRoot(this)
}