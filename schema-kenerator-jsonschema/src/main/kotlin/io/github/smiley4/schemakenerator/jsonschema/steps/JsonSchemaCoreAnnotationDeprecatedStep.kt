package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.annotations.Deprecated
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonBooleanValue
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAnnotationUtils.iterateProperties

/**
 * Sets deprecated-flag from [Deprecated] or kotlin's [Deprecated]-annotation.
 */
class JsonSchemaCoreAnnotationDeprecatedStep : AbstractJsonSchemaStep() {

    override fun process(schema: JsonSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        if (schema.json is JsonObject && schema.json.properties["deprecated"] == null) {
            determineDeprecated(schema.typeData.annotations)?.also { deprecated ->
                schema.json.properties["deprecated"] = JsonBooleanValue(deprecated)
            }
        }
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineDeprecated(propData.annotations + propTypeData.annotations)?.also { deprecated ->
                prop.properties["deprecated"] = JsonBooleanValue(deprecated)
            }
        }
    }

    private fun determineDeprecated(annotations: Collection<AnnotationData>): Boolean? {
        return determineDeprecatedCore(annotations) ?: determineDeprecatedStd(annotations)
    }

    private fun determineDeprecatedCore(annotations: Collection<AnnotationData>): Boolean? {
        return annotations
            .filter { it.name == Deprecated::class.qualifiedName }
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
