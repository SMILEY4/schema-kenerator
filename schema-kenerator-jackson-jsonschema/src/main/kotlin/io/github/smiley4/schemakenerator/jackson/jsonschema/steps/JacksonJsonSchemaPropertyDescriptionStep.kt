package io.github.smiley4.schemakenerator.jackson.jsonschema.steps

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.steps.AbstractJsonSchemaStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAnnotationUtils.iterateProperties

/**
 * Adds a description to properties according to the jackson [JsonPropertyDescription]-annotation.
 */
class JacksonJsonSchemaPropertyDescriptionStep : AbstractJsonSchemaStep() {

    override fun process(schema: JsonSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            getDescription(propData.annotations + propTypeData.annotations)?.also { description ->
                prop.properties["description"] = JsonTextValue(description)
            }
        }
    }

    private fun getDescription(annotations: Collection<AnnotationData>): String? {
        return annotations
            .filter { it.name == JsonPropertyDescription::class.qualifiedName }
            .map { it.values["value"] as String }
            .firstOrNull()
    }

}
