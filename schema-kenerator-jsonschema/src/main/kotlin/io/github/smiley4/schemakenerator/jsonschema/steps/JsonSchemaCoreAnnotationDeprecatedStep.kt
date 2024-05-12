package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.annotations.SchemaDeprecated
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonBooleanValue
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAnnotationUtils.iterateProperties

/**
 * Sets deprecated-flag from [SchemaDeprecated] or kotlin's [Deprecated]-annotation.
 */
class JsonSchemaCoreAnnotationDeprecatedStep {

    fun process(bundle: Bundle<JsonSchema>): Bundle<JsonSchema> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
    }


    private fun process(schema: JsonSchema) {
        if (schema.json is JsonObject && schema.json.properties["deprecated"] == null) {
            determineDeprecated(schema.typeData)?.also { deprecated ->
                schema.json.properties["deprecated"] = JsonBooleanValue(deprecated)
            }
        }
        iterateProperties(schema) { prop, data ->
            determineDeprecated(data)?.also { deprecated ->
                prop.properties["deprecated"] = JsonBooleanValue(deprecated)
            }
        }
    }

    private fun determineDeprecated(typeData: BaseTypeData): Boolean? {
        return determineDeprecatedCore(typeData.annotations) ?: determineDeprecatedStd(typeData.annotations)
    }

    private fun determineDeprecated(typeData: PropertyData): Boolean? {
        return determineDeprecatedCore(typeData.annotations) ?: determineDeprecatedStd(typeData.annotations)
    }

    private fun determineDeprecatedCore(annotations: Collection<AnnotationData>): Boolean? {
        return annotations
            .filter { it.name == SchemaDeprecated::class.qualifiedName }
            .map { it.values["deprecated"] as Boolean }
            .firstOrNull()
    }

    private fun determineDeprecatedStd(annotations: Collection<AnnotationData>): Boolean? {
        return if (annotations.any { it.name == Deprecated::class.qualifiedName }) {
            true
        } else {
            null
        }
    }

}