package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaCompileUtils.resolveReferences
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaCompileUtils.shouldReference
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.IntermediateJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonArray
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNullValue
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.obj

internal class JsonSchemaCompileReferenceStep(
    private val explicitNullTypes: Boolean,
    private val pathBuilder: (type: TypeData, types: Map<TypeId, TypeData>) -> String
) {

    private val schemaUtils = JsonSchemaUtils()

    private class Context(
        /**
         * all known input json schemas
         */
        val knownSchemas: List<JsonSchemaData>,
        /**
         * all known input types
         */
        val knownTypeData: Map<TypeId, TypeData>,
        /**
         *  the current list of schemas in the definitions section. Add new ones to this list.
         */
        val definitions: MutableMap<String, JsonNode>,
        /**
         * counts how often any (original) path is used in the definitions-section
         */
        val pathCounters: MutableMap<String, Int>,
        /**
         * already mapped reference paths for types. Add new paths to this map.
         */
        val refPathMapping: MutableMap<TypeId, String>,
    ) {
        companion object {
            fun from(data: IntermediateJsonSchemaData) = Context(
                data.entries,
                data.typeDataById,
                mutableMapOf(),
                mutableMapOf(),
                mutableMapOf(),
            )
        }
    }


    /**
     * Put referenced schemas into definitions and reference them
     */
    fun compile(input: IntermediateJsonSchemaData): CompiledJsonSchemaData {
        val context = Context.from(input)
        val root = resolveReferences(input.rootSchema) { refObj ->
            resolveReference(refObj, context)
        }
        return CompiledJsonSchemaData(
            typeData = input.rootTypeData,
            json = root,
            definitions = context.definitions
        )
    }


    /**
     * Handles a schema object referencing another schema using a temporary reference path.
     * @param refObj the object with the temporary reference path
     * @param context the current compile context with data about input schemas and type data as well as current produced information
     */
    private fun resolveReference(refObj: JsonObject, context: Context): JsonNode {
        // find actual schema data
        val referencedSchema = context.knownSchemas.find { it.typeData.id == TypeId((refObj.properties["\$ref"] as JsonTextValue).value) }
        if (referencedSchema == null) {
            return refObj
        }

        // create swagger property with correct reference path (and add actual schema to context)
        val property = if (shouldReference(referencedSchema.json)) {
            createRefProperty(refObj, referencedSchema, context)
        } else {
            createInlineProperty(refObj, referencedSchema)
        }

        // add back some information to property
        if (refObj.properties.containsKey("description") && property is JsonObject) {
            property.properties["description"] = refObj.properties["description"] ?: JsonNullValue()
        }

        // return
        return property
    }


    /**
     * Create a json-schema with a proper reference to replace the pending referencing schema.
     * @param schema the referenced schema
     * @param context the current compile context with data about input schemas and type data as well as current produced information
     */
    private fun createRefProperty(
        refObj: JsonObject,
        schema: JsonSchemaData,
        context: Context
    ): JsonNode {
        val refPath = if (context.refPathMapping.containsKey(schema.typeData.id)) {
            context.refPathMapping[schema.typeData.id]!!
        } else {
            var newRefPath = pathBuilder(schema.typeData, context.knownTypeData)
            context.pathCounters[newRefPath] = (context.pathCounters[newRefPath] ?: 0) + 1
            if (context.definitions.containsKey(newRefPath)) {
                newRefPath += context.pathCounters[newRefPath]
            }
            context.refPathMapping[schema.typeData.id] = newRefPath
            context.definitions[newRefPath] = placeholder() // avoid infinite recursive loops
            context.definitions[newRefPath] = resolveReferences(schema.json) { resolveReference(it, context) }
            newRefPath
        }

        return if(refObj.properties.containsKey("_nullable") && refObj.getBool("_nullable")) {
            schemaUtils.referenceSchemaNullable(refPath, true)
        } else {
            schemaUtils.referenceSchema(refPath, true)
        }
    }


    /**
     * Create an inline json-schema to replace the pending referencing schema.
     * @param refObj the schema containing the reference
     * @param schema the schema referenced by [refObj]
     */
    private fun createInlineProperty(refObj: JsonObject, schema: JsonSchemaData): JsonNode {
        return schema.json.copyNode().also {
            if (it is JsonObject) {
                it.properties.putAll(buildMap {
                    this.putAll(refObj.properties)
                    this.remove("\$ref")
                })
                if (it.properties.contains("_nullable") && it.getBool("_nullable") && explicitNullTypes) {
                    setNullable(it)
                    it.properties.remove("_nullable")
                }
            }
        }
    }

    /**
     * Explicitly mark the given schema as a nullable type
     */
    private fun setNullable(schema: JsonObject) {
        if (schema.properties.contains("type") && schema.properties["type"] is JsonTextValue) {
            schema.properties["type"] = JsonArray(
                mutableListOf(
                    JsonTextValue(schema.getText("type")),
                )
            )
        }
        if (schema.properties.contains("type") && schema.properties["type"] is JsonArray) {
            schema.getArray("type").items.add(JsonTextValue("null"))
        }
    }

    /**
     * @return a placeholder json object
     */
    private fun placeholder() = obj { }

}
