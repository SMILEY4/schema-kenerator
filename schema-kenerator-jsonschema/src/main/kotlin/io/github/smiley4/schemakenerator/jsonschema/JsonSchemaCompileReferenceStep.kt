package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.flatten
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.obj
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaCompileUtils.resolveReferences
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaCompileUtils.shouldReference

/**
 * Resolves references in prepared json-schemas by collecting them in the definitions-section and referencing them.
 * @param pathBuilder builds the path to reference the type, i.e. which "name" to use
 */
class JsonSchemaCompileReferenceStep(private val pathBuilder: (type: TypeData, types: Map<TypeId, TypeData>) -> String) {

    private val schemaUtils = JsonSchemaUtils()

    private class Context(
        /**
         * all known input json schemas
         */
        val knownSchemas: List<JsonSchema>,
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
            fun from(bundle: Bundle<JsonSchema>) = Context(
                bundle.flatten(),
                bundle.buildTypeDataMap(),
                mutableMapOf(),
                mutableMapOf(),
                mutableMapOf(),
            )
        }
    }

    /**
     * Put referenced schemas into definitions and reference them
     */
    fun compile(bundle: Bundle<JsonSchema>): CompiledJsonSchema {
        val context = Context.from(bundle)
        val root = resolveReferences(bundle.data.json) { refObj ->
            resolveReference(refObj, context)
        }
        return CompiledJsonSchema(
            typeData = bundle.data.typeData,
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
        val referencedSchema = context.knownSchemas.find(TypeId((refObj.properties["\$ref"] as JsonTextValue).value))
        return if (referencedSchema == null) {
            refObj
        } else {
            if (shouldReference(referencedSchema.json)) {
                createRefProperty(referencedSchema, context)
            } else {
                createInlineProperty(refObj, referencedSchema)
            }
        }
    }


    /**
     * Create a json-schema with a proper reference to replace the pending referencing schema.
     * @param schema the referenced schema
     * @param context the current compile context with data about input schemas and type data as well as current produced information
     */
    private fun createRefProperty(schema: JsonSchema, context: Context): JsonNode {
        val refPath = if(context.refPathMapping.containsKey(schema.typeData.id)) {
            context.refPathMapping[schema.typeData.id]!!
        } else {
            var newRefPath = pathBuilder(schema.typeData, context.knownTypeData)
            context.pathCounters[newRefPath] = (context.pathCounters[newRefPath] ?: 0) + 1
            if(context.definitions.containsKey(newRefPath)) {
                newRefPath += context.pathCounters[newRefPath]
            }
            context.refPathMapping[schema.typeData.id] = newRefPath
            context.definitions[newRefPath] = placeholder() // avoid infinite recursive loops
            context.definitions[newRefPath] = resolveReferences(schema.json) { resolveReference(it, context) }
            newRefPath
        }
        return schemaUtils.referenceSchema(refPath, true)
    }


    /**
     * Create an inline json-schema to replace the pending referencing schema.
     * @param refObj the schema containing the reference
     * @param schema the schema referenced by [refObj]
     */
    private fun createInlineProperty(refObj: JsonObject, schema: JsonSchema): JsonNode {
        return schema.json.copyNode().also {
            if (it is JsonObject) {
                it.properties.putAll(buildMap {
                    this.putAll(refObj.properties)
                    this.remove("\$ref")
                })
            }
        }
    }


    /**
     * @return a placeholder json object
     */
    private fun placeholder() = obj { }


    /**
     * @return the [JsonSchema] for the given [TypeId]
     */
    private fun Collection<JsonSchema>.find(id: TypeId): JsonSchema? = this.find { it.typeData.id == id }

}
