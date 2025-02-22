package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.annotations.Format
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.jsonschema.data.IntermediateJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue

internal class JsonSchemaCoreAnnotationFormatStep {

    fun process(input: IntermediateJsonSchemaData): IntermediateJsonSchemaData {
        input.entries.forEach { process(it, input.typeDataById) }
        return input
    }

    private fun process(schema: JsonSchemaData, typeDataMap: Map<TypeId, TypeData>) {
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
