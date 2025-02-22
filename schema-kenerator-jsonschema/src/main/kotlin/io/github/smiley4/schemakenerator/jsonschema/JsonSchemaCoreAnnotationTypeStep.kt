package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.annotations.Type
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.jsonschema.data.IntermediateJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue

internal class JsonSchemaCoreAnnotationTypeStep {

    fun process(input: IntermediateJsonSchemaData): IntermediateJsonSchemaData {
        input.entries.forEach { process(it, input.typeDataById) }
        return input
    }

    private fun process(schema: JsonSchemaData, typeDataMap: Map<TypeId, TypeData>) {
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
