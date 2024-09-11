package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileUtils.shouldReference

/**
 * Resolves references in prepared json-schemas by collecting them in the definitions-section and referencing them.
 * @param pathBuilder builds the path to reference the type, i.e. which "name" to use
 */
class JsonSchemaCompileReferenceRootStep(private val pathBuilder: (type: BaseTypeData, types: Map<TypeId, BaseTypeData>) -> String) {

    private val schemaUtils = JsonSchemaUtils()


    /**
     * Put referenced schemas into definitions and reference them
     */
    fun compile(bundle: Bundle<JsonSchema>): CompiledJsonSchema {
        val result = JsonSchemaCompileReferenceStep(pathBuilder).compile(bundle)
        if (shouldReference(result.json)) {
            val refPath = pathBuilder(result.typeData, bundle.buildTypeDataMap())
            return CompiledJsonSchema(
                typeData = result.typeData,
                json = schemaUtils.referenceSchema(refPath, true),
                definitions = buildMap {
                    this.putAll(result.definitions)
                    this[refPath] = result.json
                }
            )
        } else {
            return CompiledJsonSchema(
                typeData = result.typeData,
                json = result.json,
                definitions = result.definitions
            )
        }
    }

}
