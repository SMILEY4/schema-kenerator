package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaCompileUtils.shouldReference
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.IntermediateJsonSchemaData

internal class JsonSchemaCompileReferenceRootStep(
    private val explicitNullTypes: Boolean,
    private val pathBuilder: (type: TypeData, types: Map<TypeId, TypeData>) -> String
) {

    private val schemaUtils = JsonSchemaUtils()


    /**
     * Put referenced schemas into definitions and reference them
     */
    fun compile(input: IntermediateJsonSchemaData): CompiledJsonSchemaData {
        val result = JsonSchemaCompileReferenceStep(true, pathBuilder).compile(input)
        if (shouldReference(result.json, result.typeData)) {
            val refPath = pathBuilder(result.typeData, input.typeDataById)
            return CompiledJsonSchemaData(
                typeData = result.typeData,
                json = schemaUtils.referenceSchema(refPath, true),
                definitions = buildMap {
                    this.putAll(result.definitions)
                    this[refPath] = result.json
                }
            )
        } else {
            return CompiledJsonSchemaData(
                typeData = result.typeData,
                json = result.json,
                definitions = result.definitions
            )
        }
    }

}
