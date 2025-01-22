package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.flatten
import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.core.typedata.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.obj
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileUtils.resolveReferences
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileUtils.shouldReference
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * Resolves references in prepared json-schemas by collecting them in the definitions-section and referencing them.
 * @param pathBuilder builds the path to reference the type, i.e. which "name" to use
 */
class JsonSchemaCompileReferenceStep(private val pathBuilder: (type: TypeData, types: Map<TypeId, TypeData>) -> String) {

    private val schemaUtils = JsonSchemaUtils()


    /**
     * Put referenced schemas into definitions and reference them
     */
    fun compile(bundle: Bundle<JsonSchema>): CompiledJsonSchema {
        val knownSchemas = bundle.flatten()
        val knownTypeData = bundle.buildTypeDataMap()
        val definitions = mutableMapOf<String, JsonNode>()
        val pathCounters = mutableMapOf<String, Int>()
        val refPathMapping = mutableMapOf<TypeId, String>()

        val root = resolveReferences(bundle.data.json) { refObj ->
            resolveReference(refObj, knownSchemas, knownTypeData, refPathMapping, definitions, pathCounters)
        }

        return CompiledJsonSchema(
            typeData = bundle.data.typeData,
            json = root,
            definitions = definitions
        )
    }


    /**
     * Handles a schema object referencing another schema using a temporary reference path.
     * @param refObj the object with the temporary reference path
     * @param knownSchemas all known json schemas
     * @param knownTypeData all known types
     * @param refPathMapping already mapped reference paths for types. Add new paths to this map.
     * @param definitions the current list of schemas in the definitions section. Add new ones to this list.
     * @param pathCounters counts of often any (original) path is used in the definitions-section
     */
    private fun resolveReference(
        refObj: JsonObject,
        knownSchemas: List<JsonSchema>,
        knownTypeData: Map<TypeId, TypeData>,
        refPathMapping: MutableMap<TypeId, String>,
        definitions: MutableMap<String, JsonNode>,
        pathCounters: MutableMap<String, Int>
    ): JsonNode {
        val referencedSchema = knownSchemas.find(TypeId((refObj.properties["\$ref"] as JsonTextValue).value))
        return if (referencedSchema == null) {
            refObj
        } else {
            if (shouldReference(referencedSchema.json)) {
                createRefProperty(referencedSchema, knownSchemas, knownTypeData, refPathMapping, definitions, pathCounters)
            } else {
                createInlineProperty(refObj, referencedSchema)
            }
        }
    }


    /**
     * Create a json-schema with a proper reference to replace the pending referencing schema.
     * @param schema the referenced schema
     * @param knownSchemas all input json schemas
     * @param knownTypeData all input type data
     * @param refPathMapping already mapped reference paths for types. Add new paths to this map.
     * @param definitions json schema definitions section. Adds new referenced schemas.
     * @param pathCounters counts of often any (original) path is used in the definitions-section
     */
    private fun createRefProperty(
        schema: JsonSchema,
        knownSchemas: List<JsonSchema>,
        knownTypeData: Map<TypeId, TypeData>,
        refPathMapping: MutableMap<TypeId, String>,
        definitions: MutableMap<String, JsonNode>,
        pathCounters: MutableMap<String, Int>
    ): JsonNode {
        val refPath = if(refPathMapping.containsKey(schema.typeData.id)) {
            refPathMapping[schema.typeData.id]!!
        } else {
            var newRefPath = pathBuilder(schema.typeData, knownTypeData)
            pathCounters[newRefPath] = (pathCounters[newRefPath] ?: 0) + 1
            if(definitions.containsKey(newRefPath)) {
                newRefPath += pathCounters[newRefPath]
            }
            refPathMapping[schema.typeData.id] = newRefPath
            definitions[newRefPath] = placeholder() // avoid infinite recursive loops
            definitions[newRefPath] = resolveReferences(schema.json) { resolveReference(
                it,
                knownSchemas,
                knownTypeData,
                refPathMapping,
                definitions,
                pathCounters
            ) }
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
