package io.github.smiley4.schemakenerator.jackson.jsonschema

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.AbstractJsonSchemaStep
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.jsonschema.buildTypeDataMap

internal class JacksonJsonSchemaPropertyDescriptionStep {

    fun process(bundle: Bundle<JsonSchema>): Bundle<JsonSchema> {
        val typeDataMap = bundle.buildTypeDataMap()
        return bundle.also { schema ->
            process(schema.data, typeDataMap)
            schema.supporting.forEach { process(it, typeDataMap) }
        }
    }

    private fun process(schema: JsonSchema, typeDataMap: Map<TypeId, TypeData>) {
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
