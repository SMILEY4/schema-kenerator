package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAnnotationTypeHintStep
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
fun Bundle<BaseTypeData>.generateJsonSchema(): Bundle<JsonSchema> {
    return JsonSchemaGenerationStep().generate(this)
}


/**
 * See [JsonSchemaAutoTitleStep]
 */
fun Bundle<JsonSchema>.withAutoTitle(type: TitleType = TitleType.FULL): Bundle<JsonSchema> {
    return JsonSchemaAutoTitleStep(type).process(this)
}


/**
 * See [JsonSchemaCoreAnnotationDefaultStep]
 */
fun Bundle<JsonSchema>.handleDefaultAnnotation(): Bundle<JsonSchema> {
    return JsonSchemaCoreAnnotationDefaultStep().process(this)
}


/**
 * See [JsonSchemaCoreAnnotationDeprecatedStep]
 */
fun Bundle<JsonSchema>.handleDeprecatedAnnotation(): Bundle<JsonSchema> {
    return JsonSchemaCoreAnnotationDeprecatedStep().process(this)
}


/**
 * See [JsonSchemaCoreAnnotationDescriptionStep]
 */
fun Bundle<JsonSchema>.handleDescriptionAnnotation(): Bundle<JsonSchema> {
    return JsonSchemaCoreAnnotationDescriptionStep().process(this)
}


/**
 * See [JsonSchemaCoreAnnotationExamplesStep]
 */
fun Bundle<JsonSchema>.handleExampleAnnotation(): Bundle<JsonSchema> {
    return JsonSchemaCoreAnnotationExamplesStep().process(this)
}


/**
 * See [JsonSchemaCoreAnnotationTitleStep]
 */
fun Bundle<JsonSchema>.handleTitleAnnotation(): Bundle<JsonSchema> {
    return JsonSchemaCoreAnnotationTitleStep().process(this)
}


/**
 * See [JsonSchemaAnnotationTypeHintStep]
 */
fun Bundle<JsonSchema>.handleJsonTypeHintAnnotation(): Bundle<JsonSchema> {
    return JsonSchemaAnnotationTypeHintStep().process(this)
}


/**
 * See [JsonSchemaCompileStep]
 */
fun Bundle<JsonSchema>.compileInlining(): CompiledJsonSchema {
    return JsonSchemaCompileStep().compileInlining(this)
}


/**
 * See [JsonSchemaCompileStep]
 */
fun Bundle<JsonSchema>.compileReferencing(pathType: TitleType = TitleType.FULL): CompiledJsonSchema {
    return JsonSchemaCompileStep(pathType).compileReferencing(this)
}


/**
 * See [JsonSchemaCompileStep]
 */
fun Bundle<JsonSchema>.compileReferencingRoot(pathType: TitleType = TitleType.FULL): CompiledJsonSchema {
    return JsonSchemaCompileStep(pathType).compileReferencingRoot(this)
}
