package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.flatten
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
class JsonSchemaCompileReferenceStep(private val pathBuilder: (type: BaseTypeData, types: Map<TypeId, BaseTypeData>) -> String) {

    private val schemaUtils = JsonSchemaUtils()


    /**
     * Put referenced schemas into definitions and reference them
     */
    fun compile(bundle: Bundle<JsonSchema>): CompiledJsonSchema {
        val schemaList = bundle.flatten()
        val typeDataMap = bundle.buildTypeDataMap()
        val definitions = mutableMapOf<String, JsonNode>()

        val root = resolveReferences(bundle.data.json) { refObj ->
            resolve(refObj, schemaList, typeDataMap, definitions)
        }

        return CompiledJsonSchema(
            typeData = bundle.data.typeData,
            json = root,
            definitions = definitions
        )
    }

    private fun resolve(
        refObj: JsonObject,
        schemaList: List<JsonSchema>,
        typeDataMap: Map<TypeId, BaseTypeData>,
        definitions: MutableMap<String, JsonNode>
    ): JsonNode {
        val referencedId = TypeId.parse((refObj.properties["\$ref"] as JsonTextValue).value)
        val referencedSchema = schemaList.find(referencedId)
        return if (referencedSchema != null) {
            if (shouldReference(referencedSchema.json)) {
                val refPath = pathBuilder(referencedSchema.typeData, typeDataMap)
                if (!definitions.containsKey(refPath)) {
                    definitions[refPath] = placeholder() // break out of infinite loops
                    definitions[refPath] = resolveReferences(referencedSchema.json) { resolve(it, schemaList, typeDataMap, definitions) }
                }
                schemaUtils.referenceSchema(refPath, true)
            } else {
                referencedSchema.json.also {
                    if (it is JsonObject) {
                        it.properties.putAll(buildMap {
                            this.putAll(refObj.properties)
                            this.remove("\$ref")
                        })
                    }
                }
            }
        } else {
            refObj
        }
    }

    private fun placeholder() = obj { }

    private fun Collection<JsonSchema>.find(id: TypeId): JsonSchema? {
        return this.find { it.typeData.id == id }
    }

}
