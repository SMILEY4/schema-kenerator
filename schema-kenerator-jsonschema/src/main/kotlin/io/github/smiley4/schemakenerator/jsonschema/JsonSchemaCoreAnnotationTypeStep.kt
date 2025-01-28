package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.annotations.Type
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue

internal class JsonSchemaCoreAnnotationTypeStep {

    fun process(bundle: Bundle<JsonSchema>): Bundle<JsonSchema> {
        val typeDataMap = bundle.buildTypeDataMap()
        return bundle.also { schema ->
            process(schema.data, typeDataMap)
            schema.supporting.forEach { process(it, typeDataMap) }
        }
    }

    private fun process(schema: JsonSchema, typeDataMap: Map<TypeId, TypeData>) {
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
