package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.annotations.Format
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAnnotationUtils.iterateProperties

/**
 * Adds format values from [Format]-annotation
 */
class JsonSchemaCoreAnnotationFormatStep : AbstractJsonSchemaStep() {

    override fun process(schema: JsonSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        if (schema.json is JsonObject && schema.json.properties["format"] == null) {
            determineFormat(schema.typeData.annotations)?.also { format ->
                schema.json.properties["format"] = JsonTextValue(format)
            }
        }
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineFormat(propData.annotations + propTypeData.annotations)?.also { format ->
                prop.properties["format"] = JsonTextValue(format)
            }
        }
    }

    private fun determineFormat(annotations: List<AnnotationData>): String? {
        return annotations
            .filter { it.name == Format::class.qualifiedName }
            .map { it.values["format"] as String }
            .firstOrNull()
    }
}
