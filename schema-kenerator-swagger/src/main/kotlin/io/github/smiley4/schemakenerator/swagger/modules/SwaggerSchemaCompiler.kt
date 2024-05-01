package io.github.smiley4.schemakenerator.swagger.modules

import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.WildcardTypeData
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
                typeData = schema.typeData
            )
        }
    }

    fun compileReferencing(schemas: Collection<SwaggerSchema>): List<SwaggerSchemaWithDefinitions> {
        return schemas.map { schema ->
            val result = referenceDefinitionsReferences(schema.schema, schemas)
            SwaggerSchemaWithDefinitions(
                typeData = schema.typeData,
                schema = result.schema,
                definitions = result.definitions
            )
        }
    }

    fun compileReferencingRoot(schemas: Collection<SwaggerSchema>): List<SwaggerSchemaWithDefinitions> {
        return compileReferencing(schemas).map {
            if (shouldReference(it.schema)) {
                SwaggerSchemaWithDefinitions(
                    typeData = it.typeData,
                    schema = schema.referenceSchema(it.typeData.id, true),
                    definitions = buildMap {
                        this.putAll(it.definitions)
                        this[it.typeData.id] = it.schema
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
            val referencedSchema = schemaList.find { it.typeData.id == referencedId }!!
            inlineReferences(referencedSchema.schema, schemaList).also {
                mergeInto(refObj, it)
            }
        }
    }


    private fun referenceDefinitionsReferences(node: Schema<*>, schemaList: Collection<SwaggerSchema>): SwaggerSchemaWithDefinitions {
        val definitions = mutableMapOf<TypeId, Schema<*>>()
        val json = replaceReferences(node) { refObj ->
            val referencedId = TypeId.parse(refObj.`$ref`)
            val referencedSchema = schemaList.find { it.typeData.id == referencedId }!!
            val procReferencedSchema = referenceDefinitionsReferences(referencedSchema.schema, schemaList).also {
                mergeInto(refObj, it.schema)
            }
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
            typeData = WildcardTypeData(),
            schema = json,
            definitions = definitions
        )
    }

    private fun mergeInto(other: Schema<*>, dst: Schema<*>) {
        other.default?.also { dst.setDefault(it) }
        other.title?.also { dst.title = it }
        other.maximum?.also { dst.maximum = it }
        other.exclusiveMaximum?.also { dst.exclusiveMaximum = it }
        other.minimum?.also { dst.minimum = it }
        other.exclusiveMinimum?.also { dst.exclusiveMinimum = it }
        other.maxLength?.also { dst.maxLength = it }
        other.minLength?.also { dst.minLength = it }
        other.pattern?.also { dst.pattern = it }
        other.maxItems?.also { dst.maxItems = it }
        other.minItems?.also { dst.minItems = it }
        other.uniqueItems?.also { dst.uniqueItems = it }
        other.maxProperties?.also { dst.maxProperties = it }
        other.minProperties?.also { dst.minProperties = it }
        other.required?.also { dst.required = it }
        other.type?.also { dst.type = it }
        other.not?.also { dst.not = it }
        other.description?.also { dst.description = it }
        other.format?.also { dst.format = it }
        other.nullable?.also { dst.nullable = it }
        other.readOnly?.also { dst.readOnly = it }
        other.writeOnly?.also { dst.writeOnly = it }
        other.example?.also { dst.example = it }
        other.externalDocs?.also { dst.externalDocs = it }
        other.deprecated?.also { dst.deprecated = it }
        other.specVersion?.also { dst.specVersion = it }
        other.exclusiveMaximumValue?.also { dst.exclusiveMaximumValue = it }
        other.exclusiveMinimumValue?.also { dst.exclusiveMinimumValue = it }
        other.contains?.also { dst.contains = it }
        other.maxContains?.also { dst.maxContains = it }
        other.minContains?.also { dst.minContains = it }
        other.examples?.also { dst.examples = it }
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
