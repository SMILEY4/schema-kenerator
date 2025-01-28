package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.annotations.Default
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaAnnotationUtils.iterateProperties

/**
 * Adds default values from [Default]-annotation
 */
class JsonSchemaCoreAnnotationDefaultStep : AbstractJsonSchemaStep() {

    override fun process(schema: JsonSchema, typeDataMap: Map<TypeId, TypeData>) {
        if (schema.json is JsonObject && schema.json.properties["default"] == null) {
            determineDefault(schema.typeData.annotations)?.also { default ->
                schema.json.properties["default"] = JsonTextValue(default)
            }
        }
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineDefault(propData.annotations + propTypeData.annotations)?.also { default ->
                prop.properties["default"] = JsonTextValue(default)
            }
        }
    }

    private fun determineDefault(annotations: List<AnnotationData>): String? {
        return annotations
            .filter { it.name == Default::class.qualifiedName }
            .map { it.values["value"] as String }
            .firstOrNull()
    }
}
