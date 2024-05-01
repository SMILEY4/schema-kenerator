package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAutoTitleStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationDefaultStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationDeprecatedStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationDescriptionStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationExamplesStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationTitleStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaGenerationStep

/**
 * See [JsonSchemaGenerationStep]
 */
fun Collection<BaseTypeData>.generateJsonSchema(): Collection<JsonSchema> {
    return JsonSchemaGenerationStep().generate(this)
}


/**
 * See [JsonSchemaAutoTitleStep]
 */
fun Collection<JsonSchema>.withAutoTitle(type: TitleType = TitleType.FULL): Collection<JsonSchema> {
    return JsonSchemaAutoTitleStep(type).process(this)
}


/**
 * See [JsonSchemaCoreAnnotationDefaultStep]
 */
fun Collection<JsonSchema>.withCoreAnnotationDefault(): Collection<JsonSchema> {
    return JsonSchemaCoreAnnotationDefaultStep().process(this)
}


/**
 * See [JsonSchemaCoreAnnotationDeprecatedStep]
 */
fun Collection<JsonSchema>.withCoreAnnotationDeprecated(): Collection<JsonSchema> {
    return JsonSchemaCoreAnnotationDeprecatedStep().process(this)
}


/**
 * See [JsonSchemaCoreAnnotationDescriptionStep]
 */
fun Collection<JsonSchema>.withCoreAnnotationDescription(): Collection<JsonSchema> {
    return JsonSchemaCoreAnnotationDescriptionStep().process(this)
}


/**
 * See [JsonSchemaCoreAnnotationExamplesStep]
 */
fun Collection<JsonSchema>.withCoreAnnotationExamples(): Collection<JsonSchema> {
    return JsonSchemaCoreAnnotationExamplesStep().process(this)
}


/**
 * See [JsonSchemaCoreAnnotationTitleStep]
 */
fun Collection<JsonSchema>.withCoreAnnotationTitle(): Collection<JsonSchema> {
    return JsonSchemaCoreAnnotationTitleStep().process(this)
}


/**
 * See [JsonSchemaCompileStep]
 */
fun Collection<JsonSchema>.compileInlining(pathType: TitleType = TitleType.FULL): List<CompiledJsonSchema> {
    return JsonSchemaCompileStep(pathType).compileInlining(this)
}


/**
 * See [JsonSchemaCompileStep]
 */
fun Collection<JsonSchema>.compileReferencing(pathType: TitleType = TitleType.FULL): List<CompiledJsonSchema> {
    return JsonSchemaCompileStep(pathType).compileReferencing(this)
}


/**
 * See [JsonSchemaCompileStep]
 */
fun Collection<JsonSchema>.compileReferencingRoot(pathType: TitleType = TitleType.FULL): List<CompiledJsonSchema> {
    return JsonSchemaCompileStep(pathType).compileReferencingRoot(this)
}