package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.RefType
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAnnotationTypeHintStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAutoTitleStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileInlineStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileReferenceRootStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileReferenceStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationDefaultStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationDeprecatedStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationDescriptionStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationExamplesStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationOptionalAndRequiredStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationTitleStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCustomizeStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaGenerationStep

enum class OptionalHandling {
    REQUIRED,
    NON_REQUIRED
}

class JsonSchemaGenerationStepConfig {
    /**
     * How to handle optional properties
     *
     * Example:
     * ```
     * class MyExample(val someValue: String = "hello")
     * ```
     * - with `optionalHandling = REQUIRED` => "someValue" is required (because is not nullable)
     * - with `optionalHandling = NON_REQUIRED` => "someValue" is not required (because a default value is provided)
     */
    var optionalHandling = OptionalHandling.REQUIRED
}


/**
 * See [JsonSchemaGenerationStep]
 */
fun Bundle<BaseTypeData>.generateJsonSchema(configBlock: JsonSchemaGenerationStepConfig.() -> Unit = {}): Bundle<JsonSchema> {
    val config = JsonSchemaGenerationStepConfig().apply(configBlock)
    return JsonSchemaGenerationStep(
        optionalAsNonRequired = config.optionalHandling == OptionalHandling.NON_REQUIRED,
    ).generate(this)
}


/**
 * See [JsonSchemaAutoTitleStep]
 */
fun Bundle<JsonSchema>.withAutoTitle(type: TitleType = TitleType.FULL): Bundle<JsonSchema> {
    return JsonSchemaAutoTitleStep(type).process(this)
}


/**
 * See [JsonSchemaCoreAnnotationDefaultStep], [JsonSchemaCoreAnnotationDeprecatedStep], [JsonSchemaCoreAnnotationDescriptionStep],
 * [JsonSchemaCoreAnnotationExamplesStep], [JsonSchemaCoreAnnotationTitleStep], [JsonSchemaCoreAnnotationOptionalAndRequiredStep]
 */
fun Bundle<JsonSchema>.handleCoreAnnotations(): Bundle<JsonSchema> {
    return this
        .let { JsonSchemaCoreAnnotationOptionalAndRequiredStep().process(this) }
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
 * See [JsonSchemaCompileInlineStep]
 */
fun Bundle<JsonSchema>.compileInlining(): CompiledJsonSchema {
    return JsonSchemaCompileInlineStep().compile(this)
}


/**
 * See [JsonSchemaCompileReferenceStep]
 */
fun Bundle<JsonSchema>.compileReferencing(pathType: RefType = RefType.FULL): CompiledJsonSchema {
    return JsonSchemaCompileReferenceStep(pathType).compile(this)
}


/**
 * See [JsonSchemaCompileReferenceRootStep]
 */
fun Bundle<JsonSchema>.compileReferencingRoot(pathType: RefType = RefType.FULL): CompiledJsonSchema {
    return JsonSchemaCompileReferenceRootStep(pathType).compile(this)
}


/**
 * See [JsonSchemaCustomizeStep.customizeTypes]
 */
fun Bundle<JsonSchema>.customizeTypes(action: (typeData: BaseTypeData, typeSchema: JsonNode) -> Unit): Bundle<JsonSchema> {
    return JsonSchemaCustomizeStep().customizeTypes(this, action)
}


/**
 * See [JsonSchemaCustomizeStep.customizeProperties]
 */
fun Bundle<JsonSchema>.customizeProperties(action: (propertyData: PropertyData, propertySchema: JsonNode) -> Unit): Bundle<JsonSchema> {
    return JsonSchemaCustomizeStep().customizeProperties(this, action)
}
