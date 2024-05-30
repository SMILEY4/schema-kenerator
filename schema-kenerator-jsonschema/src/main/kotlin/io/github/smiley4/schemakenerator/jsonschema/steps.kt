package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.RefType
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
 * See [JsonSchemaCoreAnnotationDefaultStep], [JsonSchemaCoreAnnotationDeprecatedStep], [JsonSchemaCoreAnnotationDescriptionStep],
 * [JsonSchemaCoreAnnotationExamplesStep], [JsonSchemaCoreAnnotationTitleStep]
 */
fun Bundle<JsonSchema>.handleCoreAnnotations(): Bundle<JsonSchema> {
    return this
        .let { JsonSchemaCoreAnnotationDefaultStep().process(this) }
        .let { JsonSchemaCoreAnnotationDeprecatedStep().process(this) }
        .let { JsonSchemaCoreAnnotationDescriptionStep().process(this) }
        .let { JsonSchemaCoreAnnotationExamplesStep().process(this) }
        .let { JsonSchemaCoreAnnotationTitleStep().process(this) }
}


/**
 * See [JsonSchemaAnnotationTypeHintStep]
 */
fun Bundle<JsonSchema>.handleJsonSchemaAnnotations(): Bundle<JsonSchema> {
    return this
        .let { JsonSchemaAnnotationTypeHintStep().process(this) }
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
fun Bundle<JsonSchema>.compileReferencing(pathType: RefType = RefType.FULL): CompiledJsonSchema {
    return JsonSchemaCompileStep(pathType).compileReferencing(this)
}


/**
 * See [JsonSchemaCompileStep]
 */
fun Bundle<JsonSchema>.compileReferencingRoot(pathType: RefType = RefType.FULL): CompiledJsonSchema {
    return JsonSchemaCompileStep(pathType).compileReferencingRoot(this)
}
