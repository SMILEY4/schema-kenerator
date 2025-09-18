package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.annotations.Example
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.jsonschema.data.IntermediateJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonArray
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue

/**
 * Handles the core [Example] annotation
 */
internal class JsonSchemaCoreAnnotationExamplesStep {

    fun process(input: IntermediateJsonSchemaData): IntermediateJsonSchemaData {
        input.entries.forEach { process(it, input.typeDataById) }
        return input
    }

    private fun process(schema: JsonSchemaData, typeDataMap: Map<TypeId, TypeData>) {
        if (schema.json is JsonObject && schema.json.properties["examples"] == null) {
            determineExamples(schema.typeData.annotations)?.also { examples ->
                schema.json.properties["examples"] = JsonArray().also { arr -> arr.items.addAll(examples.map { JsonTextValue(it) }) }
            }
        }
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineExamples(propData.annotations + propTypeData.annotations)?.also { examples ->
                prop.properties["examples"] = JsonArray().also { arr -> arr.items.addAll(examples.map { JsonTextValue(it) }) }
            }
        }
    }

    private fun determineExamples(annotations: Collection<AnnotationData>): List<String>? {
        return annotations
            .filter { it.name == Example::class.qualifiedName }
            .map { it.values["example"] as String }
            .let {
                it.ifEmpty {
                    null
                }
            }
    }

}
