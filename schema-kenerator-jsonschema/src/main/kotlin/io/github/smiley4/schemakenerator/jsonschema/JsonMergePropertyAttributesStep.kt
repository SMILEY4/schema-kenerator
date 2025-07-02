package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.IntermediateJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import kotlin.collections.forEach

internal class JsonMergePropertyAttributesStep {

    fun process(input: IntermediateJsonSchemaData): IntermediateJsonSchemaData {
        val open = input.entries.toMutableList()
        val content = input.data.toMutableMap()

        while (open.isNotEmpty()) {
            val current = open.removeFirst()
            process(current, content).forEach {
                content[it.typeData.id] = it
                open.add(it)
            }
        }

        return IntermediateJsonSchemaData(
            rootId = input.rootId,
            data = content
        )
    }

    private fun process(schema: JsonSchemaData, schemas: Map<TypeId, JsonSchemaData>): List<JsonSchemaData> {
        val resultingSchemas = mutableListOf<JsonSchemaData>()
        if (schema.json is JsonObject && schema.json.properties.containsKey("properties")) {
            (schema.json.properties["properties"] as JsonObject).properties
                .filterValues { property -> property is JsonObject && property.properties.containsKey("${'$'}ref") }
                .forEach { propKey, property ->
                    val propertyData = schemas[TypeId((property as JsonObject).getText("${'$'}ref"))]
                    if (propertyData != null && propertyData.json is JsonObject && needsDerivative(property, propertyData.json)) {
                        val derivedId = TypeId.Companion.create()
                        val derivedPropertyTypeData = propertyData.typeData.copy(derivedId)
                        val derivedPropertySchema = propertyData.json.copyNode() as JsonObject
                        mergeInto(property, derivedPropertySchema)
                        resultingSchemas.add(JsonSchemaData(derivedPropertySchema, derivedPropertyTypeData))
                        property.properties["${'$'}ref"] = JsonTextValue(derivedId.id)
                    }
                }
        }
        return resultingSchemas
    }


    @Suppress("CyclomaticComplexMethod", "ReturnCount")
    private fun needsDerivative(property: JsonObject, schema: JsonObject): Boolean {
        if (exists(property, "additionalProperties")) return true
        if (exists(property, "allOf")) return true
        if (exists(property, "anyOf")) return true
        if (different(property, schema, "deprecated")) return true
        if (different(property, schema, "enum")) return true
        if (different(property, schema, "example")) return true
        if (different(property, schema, "examples")) return true
        if (different(property, schema, "exclusiveMaximum")) return true
        if (different(property, schema, "exclusiveMaximumValue")) return true
        if (different(property, schema, "exclusiveMinimum")) return true
        if (different(property, schema, "exclusiveMinimumValue")) return true
        if (exists(property, "items")) return true
        if (different(property, schema, "maxContains")) return true
        if (different(property, schema, "maxItems")) return true
        if (different(property, schema, "maxLength")) return true
        if (different(property, schema, "maximum")) return true
        if (different(property, schema, "minContains")) return true
        if (different(property, schema, "minItems")) return true
        if (different(property, schema, "minLength")) return true
        if (different(property, schema, "minimum")) return true
        if (different(property, schema, "multipleOf")) return true
        if (different(property, schema, "name")) return true
        if (exists(property, "not")) return true
        if (exists(property, "oneOf")) return true
        if (different(property, schema, "pattern")) return true
        if (exists(property, "properties")) return true
        if (different(property, schema, "required")) return true
        if (different(property, schema, "const")) return true
        if (different(property, schema, "default")) return true
        if (different(property, schema, "title")) return true
        if (different(property, schema, "type")) return true
        if (different(property, schema, "uniqueItems")) return true
        return false
    }

    private fun exists(property: JsonObject, name: String): Boolean {
        return property.properties.containsKey(name)
    }

    private fun different(property: JsonObject, schema: JsonObject, name: String): Boolean {
        return property.properties.containsKey(name) && property.properties[name] != schema.properties[name]
    }

    private fun mergeInto(source: JsonObject, target: JsonObject) {
        val dontCopy = setOf("${'$'}ref")
        target.properties.putAll(source.properties.filterKeys { !dontCopy.contains(it) })
    }

}
