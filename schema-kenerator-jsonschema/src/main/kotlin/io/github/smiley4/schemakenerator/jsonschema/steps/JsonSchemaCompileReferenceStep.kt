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

        val root = resolveReferences(bundle.data.json) { refObj ->
            resolveReference(refObj, knownSchemas, knownTypeData, definitions)
        }

        return CompiledJsonSchema(
            typeData = bundle.data.typeData,
            json = root,
            definitions = definitions
        )
    }

    private fun resolveReference(
        refObj: JsonObject,
        knownSchemas: List<JsonSchema>,
        knownTypeData: Map<TypeId, TypeData>,
        definitions: MutableMap<String, JsonNode>
    ): JsonNode {
        val referencedSchema = knownSchemas.find(TypeId((refObj.properties["\$ref"] as JsonTextValue).value))
        return if (referencedSchema == null) {
            refObj
        } else {
            if (shouldReference(referencedSchema.json)) {
                createReferencing(referencedSchema, knownSchemas, knownTypeData, definitions)
            } else {
                createInlining(refObj, referencedSchema)
            }
        }
    }


    /**
     * Create a json-schema with a proper reference to replace the pending referencing schema.
     * @param schema the referenced schema
     * @param knownSchemas all input json schemas
     * @param knownTypeData all input type data
     * @param definitions json schema definitions section. Adds new referenced schemas.
     */
    private fun createReferencing(
        schema: JsonSchema,
        knownSchemas: List<JsonSchema>,
        knownTypeData: Map<TypeId, TypeData>,
        definitions: MutableMap<String, JsonNode>,
    ): JsonNode {
        val refPath = pathBuilder(schema.typeData, knownTypeData)
        if (!definitions.containsKey(refPath)) {
            definitions[refPath] = placeholder() // avoid infinite recursive loops
            definitions[refPath] = resolveReferences(schema.json) { resolveReference(it, knownSchemas, knownTypeData, definitions) }
        }
        return schemaUtils.referenceSchema(refPath, true)
    }


    /**
     * Create an inline json-schema to replace the pending referencing schema.
     * @param refObj the schema containing the reference
     * @param schema the schema referenced by [refObj]
     */
    private fun createInlining(refObj: JsonObject, schema: JsonSchema): JsonNode {
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
