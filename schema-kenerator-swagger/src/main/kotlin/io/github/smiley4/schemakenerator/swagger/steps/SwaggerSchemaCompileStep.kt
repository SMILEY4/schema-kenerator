package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.WildcardTypeData
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.swagger.v3.oas.models.media.Schema

/**
 * Resolves references in prepared swagger-schemas - either inlining them or collecting them in the components-section and referencing them.
 * @param pathType how to reference the type, i.e. which name to use
 */
class SwaggerSchemaCompileStep(private val pathType: RefType = RefType.FULL) {

    private val schemaUtils = SwaggerSchemaUtils()


    /**
     * Inline all referenced schema
     */
    fun compileInlining(bundle: Bundle<SwaggerSchema>): CompiledSwaggerSchema {
        return CompiledSwaggerSchema(
            swagger = inlineReferences(bundle.data.swagger, bundle.supporting),
            typeData = bundle.data.typeData,
            componentSchemas = emptyMap()
        )
    }

    private fun inlineReferences(node: Schema<*>, schemaList: Collection<SwaggerSchema>): Schema<*> {
        return replaceReferences(node) { refObj ->
            val referencedId = TypeId.parse(refObj.`$ref`)
            val referencedSchema = schemaList.find { it.typeData.id == referencedId }!!
            inlineReferences(referencedSchema.swagger, schemaList).also {
                mergeInto(refObj, it)
            }
        }
    }

    /**
     * Put referenced schemas into definitions and reference them
     */
    fun compileReferencing(bundle: Bundle<SwaggerSchema>): CompiledSwaggerSchema {
        val result = referenceDefinitionsReferences(bundle, bundle.data.swagger, bundle.supporting)
        return CompiledSwaggerSchema(
            typeData = bundle.data.typeData,
            swagger = result.swagger,
            componentSchemas = result.componentSchemas
        )
    }

    /**
     * Put referenced schemas and root-schema into definitions and reference them
     */
    fun compileReferencingRoot(bundle: Bundle<SwaggerSchema>): CompiledSwaggerSchema {
            val result = compileReferencing(bundle)
            return if (shouldReference(bundle.data.swagger)) {
                CompiledSwaggerSchema(
                    typeData = result.typeData,
                    swagger = schemaUtils.referenceSchema(getRefPath(result.typeData, bundle.buildTypeDataMap()), true),
                    componentSchemas = buildMap {
                        this.putAll(result.componentSchemas)
                        this[getRefPath(result.typeData, bundle.buildTypeDataMap())] = result.swagger
                    }
                )
            } else {
                result
            }
    }


    private fun referenceDefinitionsReferences(bundle: Bundle<SwaggerSchema>, node: Schema<*>, schemaList: Collection<SwaggerSchema>): CompiledSwaggerSchema {
        val definitions = mutableMapOf<String, Schema<*>>()
        val json = replaceReferences(node) { refObj ->
            val referencedId = TypeId.parse(refObj.`$ref`.replace("#/components/schemas/", ""))
            val referencedSchema = schemaList.find { it.typeData.id == referencedId }!!
            val procReferencedSchema = referenceDefinitionsReferences(bundle, referencedSchema.swagger, schemaList).also {
                mergeInto(refObj, it.swagger)
            }
            if (shouldReference(referencedSchema.swagger)) {
                definitions[getRefPath(referencedSchema.typeData, bundle.buildTypeDataMap())] = procReferencedSchema.swagger
                definitions.putAll(procReferencedSchema.componentSchemas)
                schemaUtils.referenceSchema(getRefPath(referencedSchema.typeData, bundle.buildTypeDataMap()), true)
            } else {
                definitions.putAll(procReferencedSchema.componentSchemas)
                procReferencedSchema.swagger
            }
        }
        return CompiledSwaggerSchema(
            typeData = WildcardTypeData(),
            swagger = json,
            componentSchemas = definitions
        )
    }

    @Suppress("CyclomaticComplexMethod")
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
        other.examples?.also {
            @Suppress("UNCHECKED_CAST")
            (dst as Schema<Any?>).examples = it
        }
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

//    private fun getRefPath(typeId: TypeId): String {
//        return when (pathType) {
//            RefType.FULL -> typeId.full()
//            RefType.SIMPLE -> typeId.simple()
//        }
//    }

    private fun getRefPath(typeData: BaseTypeData, typeDataMap: Map<TypeId, BaseTypeData>): String {
        return when (pathType) {
            RefType.FULL -> typeData.qualifiedName
            RefType.SIMPLE -> typeData.simpleName
        }.let {
            if (typeData.typeParameters.isNotEmpty()) {
                val paramString = typeData.typeParameters
                    .map { (_, param) -> getRefPath(typeDataMap[param.type]!!, typeDataMap) }
                    .joinToString(",")
                "$it<$paramString>"
            } else {
                it
            }
        }.let {
            it + (typeData.id.additionalId?.let { a -> "#$a" } ?: "")
        }
    }

}
