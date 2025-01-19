package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.annotations.Type
import old.AnnotationData
import old.BaseTypeData
import old.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAnnotationUtils.iterateProperties

/**
 * Adds format values from [Type]-annotation
 */
class JsonSchemaCoreAnnotationTypeStep : AbstractJsonSchemaStep() {

    override fun process(schema: JsonSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        if (schema.json is JsonObject && schema.json.properties["type"] == null) {
            determineType(schema.typeData.annotations)?.also { type ->
                schema.json.properties["type"] = JsonTextValue(type)
            }
        }
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineType(propData.annotations + propTypeData.annotations)?.also { type ->
                prop.properties["type"] = JsonTextValue(type)
            }
        }
    }

    private fun determineType(annotations: List<AnnotationData>): String? {
        return annotations
            .filter { it.name == Type::class.qualifiedName }
            .map { it.values["type"] as String }
            .firstOrNull()
    }
}
