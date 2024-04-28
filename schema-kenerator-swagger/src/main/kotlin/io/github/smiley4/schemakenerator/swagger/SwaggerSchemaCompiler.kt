package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.schema.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.schema.SwaggerSchemaUtils
import io.github.smiley4.schemakenerator.swagger.schema.SwaggerSchemaWithDefinitions
import io.swagger.v3.oas.models.media.Schema

/**
 * Resolves references in schemas - either inlining them or collecting them in the definitions-section and referncing them.
 */
class SwaggerSchemaCompiler {

    private val schema = SwaggerSchemaUtils()

    fun compileInlining(schemas: Collection<SwaggerSchema>): List<SwaggerSchema> {
        return schemas.map { schema ->
            SwaggerSchema(
                schema = inlineReferences(schema.schema, schemas),
                typeId = schema.typeId
            )
        }
    }

    fun compileReferencing(schemas: Collection<SwaggerSchema>): List<SwaggerSchemaWithDefinitions> {
        return schemas.map { schema ->
            val result = referenceDefinitionsReferences(schema.schema, schemas)
            SwaggerSchemaWithDefinitions(
                typeId = schema.typeId,
                schema = result.schema,
                definitions = result.definitions
            )
        }
    }

    fun compileReferencingRoot(schemas: Collection<SwaggerSchema>): List<SwaggerSchemaWithDefinitions> {
        return compileReferencing(schemas).map {
            if (shouldReference(it.schema)) {
                SwaggerSchemaWithDefinitions(
                    typeId = it.typeId,
                    schema = schema.referenceSchema(it.typeId, true),
                    definitions = buildMap {
                        this.putAll(it.definitions)
                        this[it.typeId] = it.schema
                    }
                )
            } else {
                it
            }
        }
    }

    private fun inlineReferences(node: Schema<*>, schemaList: Collection<SwaggerSchema>): Schema<*> {
        return replaceReferences(node) { refObj ->
            val referencedId = TypeId.parse(refObj.`$ref`)
            val referencedSchema = schemaList.find { it.typeId == referencedId }!!
            inlineReferences(referencedSchema.schema, schemaList)
        }
    }

    private fun referenceDefinitionsReferences(node: Schema<*>, schemaList: Collection<SwaggerSchema>): SwaggerSchemaWithDefinitions {
        val definitions = mutableMapOf<TypeId, Schema<*>>()
        val json = replaceReferences(node) { refObj ->
            val referencedId = TypeId.parse(refObj.`$ref`)
            val referencedSchema = schemaList.find { it.typeId == referencedId }!!
            val procReferencedSchema = referenceDefinitionsReferences(referencedSchema.schema, schemaList)
            if (shouldReference(referencedSchema.schema)) {
                definitions[referencedId] = procReferencedSchema.schema
                definitions.putAll(procReferencedSchema.definitions)
                schema.referenceSchema(referencedId, true)
            } else {
                definitions.putAll(procReferencedSchema.definitions)
                procReferencedSchema.schema
            }
        }
        return SwaggerSchemaWithDefinitions(
            typeId = TypeId.unknown(),
            schema = json,
            definitions = definitions
        )
    }

    private fun shouldReference(schema: Schema<*>): Boolean {
        return (schema.type == "object" && schema.properties != null)
                || schema.enum != null
                || schema.anyOf != null

    }

    private fun replaceReferences(node: Schema<*>, mapping: (refObj: Schema<*>) -> Schema<*>): Schema<*> {
        if (node.`$ref` != null) {
            return mapping(node)
        } else {
            // todo: clone node ?
            return node.also { n ->
                n.items = n.items?.let { replaceReferences(it, mapping) }
                n.properties = n.properties?.let { it.mapValues { e -> replaceReferences(e.value, mapping) } }
                n.additionalItems = n.additionalItems?.let { replaceReferences(it, mapping) }
                n.additionalProperties = n.additionalProperties?.let { replaceReferences(it as Schema<*>, mapping) }
                n.allOf = n.allOf?.let { it.map { e -> replaceReferences(e, mapping) } }
                n.anyOf = n.anyOf?.let { it.map { e -> replaceReferences(e, mapping) } }
                n.oneOf = n.oneOf?.let { it.map { e -> replaceReferences(e, mapping) } }
            }
        }
    }

}
