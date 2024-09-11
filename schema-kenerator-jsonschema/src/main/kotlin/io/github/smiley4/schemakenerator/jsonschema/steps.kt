package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.RefType
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAnnotationTypeHintStep
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
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaTitleStep
import io.github.smiley4.schemakenerator.jsonschema.steps.TitleBuilder

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
 * See [JsonSchemaTitleStep]
 */
@Deprecated("Was renamed", ReplaceWith("withTitle"))
fun Bundle<JsonSchema>.withAutoTitle(type: TitleType = TitleType.FULL): Bundle<JsonSchema> {
    return withTitle(type)
}


/**
 * See [JsonSchemaTitleStep]
 */
fun Bundle<JsonSchema>.withTitle(type: TitleType = TitleType.FULL): Bundle<JsonSchema> {
    return withTitle(
        when (type) {
            TitleType.FULL -> TitleBuilder.BUILDER_FULL
            TitleType.SIMPLE -> TitleBuilder.BUILDER_SIMPLE
        }
    )
}


/**
 * See [JsonSchemaTitleStep]
 */
fun Bundle<JsonSchema>.withTitle(builder: (type: BaseTypeData, types: Map<TypeId, BaseTypeData>) -> String): Bundle<JsonSchema> {
    return JsonSchemaTitleStep(builder).process(this)
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
    return compileReferencing(
        when (pathType) {
            RefType.FULL -> TitleBuilder.BUILDER_FULL
            RefType.SIMPLE -> TitleBuilder.BUILDER_SIMPLE
        }
    )
}


/**
 * See [JsonSchemaCompileReferenceStep]
 */
fun Bundle<JsonSchema>.compileReferencing(builder: (type: BaseTypeData, types: Map<TypeId, BaseTypeData>) -> String): CompiledJsonSchema {
    return JsonSchemaCompileReferenceStep(builder).compile(this)
}


/**
 * See [JsonSchemaCompileReferenceRootStep]
 */
fun Bundle<JsonSchema>.compileReferencingRoot(pathType: RefType = RefType.FULL): CompiledJsonSchema {
    return compileReferencingRoot(
        when (pathType) {
            RefType.FULL -> TitleBuilder.BUILDER_FULL
            RefType.SIMPLE -> TitleBuilder.BUILDER_SIMPLE
        }
    )
}


/**
 * See [JsonSchemaCompileReferenceRootStep]
 */
fun Bundle<JsonSchema>.compileReferencingRoot(
    builder: (type: BaseTypeData, types: Map<TypeId, BaseTypeData>) -> String
): CompiledJsonSchema {
    return JsonSchemaCompileReferenceRootStep(builder).compile(this)
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
