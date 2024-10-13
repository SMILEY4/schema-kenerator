package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.annotations.Example
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonArray
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAnnotationUtils.iterateProperties

/**
 * Adds example-values from [Example]-annotations.
 */
class JsonSchemaCoreAnnotationExamplesStep : AbstractJsonSchemaStep() {

    override fun process(schema: JsonSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
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
