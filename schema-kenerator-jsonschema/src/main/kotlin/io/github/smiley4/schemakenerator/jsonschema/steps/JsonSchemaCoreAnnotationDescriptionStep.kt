package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.annotations.Description
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAnnotationUtils.iterateProperties

/**
 * Adds a description from the [Description]-annotation.
 */
class JsonSchemaCoreAnnotationDescriptionStep : AbstractJsonSchemaStep() {

    override fun process(schema: JsonSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        if (schema.json is JsonObject && schema.json.properties["description"] == null) {
            determineDescription(schema.typeData.annotations)?.also { description ->
                schema.json.properties["description"] = JsonTextValue(description)
            }
        }
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineDescription(propData.annotations + propTypeData.annotations)?.also { description ->
                prop.properties["description"] = JsonTextValue(description)
            }
        }
    }

    private fun determineDescription(annotations: Collection<AnnotationData>): String? {
        return annotations
            .filter { it.name == Description::class.qualifiedName }
            .map { it.values["description"] as String }
            .firstOrNull()
    }

}
