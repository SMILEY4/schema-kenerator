package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.typedata.MemberData
import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.core.typedata.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.RefType
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileInlineStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileReferenceRootStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileReferenceStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationDefaultStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationDeprecatedStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationDescriptionStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationExamplesStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationFormatStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationOptionalAndRequiredStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationTitleStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationTypeStep
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
fun Bundle<TypeData>.generateJsonSchema(configBlock: JsonSchemaGenerationStepConfig.() -> Unit = {}): Bundle<JsonSchema> {
    val config = JsonSchemaGenerationStepConfig().apply(configBlock)
    return JsonSchemaGenerationStep(
        optionalAsNonRequired = config.optionalHandling == OptionalHandling.NON_REQUIRED,
    ).process(this)
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
fun Bundle<JsonSchema>.withTitle(builder: (type: TypeData, types: Map<TypeId, TypeData>) -> String): Bundle<JsonSchema> {
    return JsonSchemaTitleStep(builder).process(this)
}


/**
 * See [JsonSchemaCoreAnnotationDefaultStep], [JsonSchemaCoreAnnotationDeprecatedStep], [JsonSchemaCoreAnnotationDescriptionStep],
 * [JsonSchemaCoreAnnotationExamplesStep], [JsonSchemaCoreAnnotationTitleStep], [JsonSchemaCoreAnnotationOptionalAndRequiredStep],
 * [JsonSchemaCoreAnnotationFormatStep], [JsonSchemaCoreAnnotationTypeStep]
 */
fun Bundle<JsonSchema>.handleCoreAnnotations(): Bundle<JsonSchema> {
    return this
        .let { JsonSchemaCoreAnnotationOptionalAndRequiredStep().process(this) }
        .let { JsonSchemaCoreAnnotationDefaultStep().process(this) }
        .let { JsonSchemaCoreAnnotationDeprecatedStep().process(this) }
        .let { JsonSchemaCoreAnnotationDescriptionStep().process(this) }
        .let { JsonSchemaCoreAnnotationExamplesStep().process(this) }
        .let { JsonSchemaCoreAnnotationTitleStep().process(this) }
        .let { JsonSchemaCoreAnnotationFormatStep().process(this) }
        .let { JsonSchemaCoreAnnotationTypeStep().process(this) }
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
fun Bundle<JsonSchema>.compileReferencing(builder: (type: TypeData, types: Map<TypeId, TypeData>) -> String): CompiledJsonSchema {
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
    builder: (type: TypeData, types: Map<TypeId, TypeData>) -> String
): CompiledJsonSchema {
    return JsonSchemaCompileReferenceRootStep(builder).compile(this)
}


/**
 * See [JsonSchemaCustomizeStep.customizeTypes]
 */
fun Bundle<JsonSchema>.customizeTypes(action: (typeData: TypeData, typeSchema: JsonNode) -> Unit): Bundle<JsonSchema> {
    return JsonSchemaCustomizeStep().customizeTypes(this, action)
}


/**
 * See [JsonSchemaCustomizeStep.customizeProperties]
 */
fun Bundle<JsonSchema>.customizeProperties(action: (memberData: MemberData, propertySchema: JsonNode) -> Unit): Bundle<JsonSchema> {
    return JsonSchemaCustomizeStep().customizeProperties(this, action)
}
