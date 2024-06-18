package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.swagger.v3.oas.models.media.Schema

object SwaggerSchemaCompileUtils {

    /**
     * Whether the given schema should be referenced or inlined
     */
    fun shouldReference(schema: Schema<*>): Boolean {
        return (schema.type == "object" && schema.properties != null)
                || schema.enum != null
                || schema.anyOf != null

    }


    /**
     * Create a name or path as key into the schema components for the given type
     */
    fun getRefPath(pathType: RefType, typeData: BaseTypeData, typeDataMap: Map<TypeId, BaseTypeData>): String {
        return when (pathType) {
            RefType.FULL -> typeData.qualifiedName
            RefType.SIMPLE -> typeData.simpleName
        }.let {
            if (typeData.typeParameters.isNotEmpty()) {
                val paramString = typeData.typeParameters
                    .map { (_, param) -> getRefPath(pathType, typeDataMap[param.type]!!, typeDataMap) }
                    .joinToString(",")
                "$it<$paramString>"
            } else {
                it
            }
        }.let {
            it + (typeData.id.additionalId?.let { a -> "#$a" } ?: "")
        }
    }

    private const val MAX_RESOLVE_REFS_DEPTH = 64;


    /**
     * Iterates over the schema and calls the given resolver-function for all objects with a set reference.
     * The object is replaced with the object returned by the function and checked for further nested references.
     */
    fun resolveReferences(node: Schema<*>, depth: Int = 0, resolver: (refObj: Schema<*>) -> Schema<*>): Schema<*> {
        if (depth > MAX_RESOLVE_REFS_DEPTH) {
            return node;
        }
        if (node.`$ref` != null) {
            return resolveReferences(resolver(node), depth + 1, resolver)
        } else {
            node.items = node.items?.let { resolveReferences(it, depth + 1, resolver) }
            node.properties = node.properties?.let { it.mapValues { prop -> resolveReferences(prop.value, depth + 1, resolver) } }
            node.additionalItems = node.additionalItems?.let { resolveReferences(it, depth + 1, resolver) }
            node.additionalProperties = node.additionalProperties?.let { resolveReferences(it as Schema<*>, depth + 1, resolver) }
            node.allOf = node.allOf?.let { it.map { e -> resolveReferences(e, depth + 1, resolver) } }
            node.anyOf = node.anyOf?.let { it.map { e -> resolveReferences(e, depth + 1, resolver) } }
            node.oneOf = node.oneOf?.let { it.map { e -> resolveReferences(e, depth + 1, resolver) } }
            return node
        }
    }


    /**
     * Merges the present properties of "source" with the properties of "target" and returns the result as a new schema.
     */
    fun merge(source: Schema<*>, target: Schema<*>): Schema<*> {
        return copy(target).also { mergeInto(source, it) }
    }


    /**
     * Merges the given schemas by modifying the given target schema. Copies all present values from the source to the target.
     */
    @Suppress("CyclomaticComplexMethod")
    fun mergeInto(source: Schema<*>, target: Schema<*>) {
        source.additionalProperties?.also { target.additionalProperties = it }
        source.allOf?.also { target.allOf = it }
        source.anyOf?.also { target.anyOf = it }
        source.const?.also { target.setConst(it) }
        source.contains?.also { target.contains = it }
        source.default?.also { target.setDefault(it) }
        source.deprecated?.also { target.deprecated = it }
        source.description?.also { target.description = it }
        source.discriminator?.also { target.discriminator = it }
        source.enum?.also {
            @Suppress("TYPE_MISMATCH_WARNING")
            target.enum = it
        }
        source.example?.also { target.example = it }
        source.examples?.also {
            @Suppress("UNCHECKED_CAST")
            (target as Schema<Any?>).examples = it
        }
        source.exclusiveMaximum?.also { target.exclusiveMaximum = it }
        source.exclusiveMaximumValue?.also { target.exclusiveMaximumValue = it }
        source.exclusiveMinimum?.also { target.exclusiveMinimum = it }
        source.exclusiveMinimumValue?.also { target.exclusiveMinimumValue = it }
        source.externalDocs?.also { target.externalDocs = it }
        source.format?.also { target.format = it }
        source.items?.also { target.items = it }
        source.maxContains?.also { target.maxContains = it }
        source.maxItems?.also { target.maxItems = it }
        source.maxLength?.also { target.maxLength = it }
        source.maxProperties?.also { target.maxProperties = it }
        source.maximum?.also { target.maximum = it }
        source.minContains?.also { target.minContains = it }
        source.minItems?.also { target.minItems = it }
        source.minLength?.also { target.minLength = it }
        source.minProperties?.also { target.minProperties = it }
        source.minimum?.also { target.minimum = it }
        source.multipleOf?.also { target.multipleOf = it }
        source.name?.also { target.name = it }
        source.not?.also { target.not = it }
        source.nullable?.also { target.nullable = it }
        source.oneOf?.also { target.oneOf = it }
        source.pattern?.also { target.pattern = it }
        source.properties?.also { target.properties.putAll(it) }
        source.readOnly?.also { target.readOnly = it }
        source.required?.also { target.required = it }
        source.specVersion?.also { target.specVersion = it }
        source.title?.also { target.title = it }
        source.type?.also { target.type = it }
        source.uniqueItems?.also { target.uniqueItems = it }
        source.writeOnly?.also { target.writeOnly = it }
        source.xml?.also { target.xml = it }
    }


    /**
     * Creates a shallow copy of the given schema.
     */
    @Suppress("CyclomaticComplexMethod")
    private fun copy(source: Schema<*>): Schema<*> {
        return Schema<Any>().also { copy ->
            copy.additionalProperties = source.additionalProperties
            copy.allOf = source.allOf
            copy.anyOf = source.anyOf
            copy.contains = source.contains
            copy.deprecated = source.deprecated
            copy.description = source.description
            copy.discriminator = source.discriminator
            copy.enum = source.enum
            copy.example = source.example
            copy.exampleSetFlag = source.exampleSetFlag
            copy.examples = source.examples
            copy.exclusiveMaximum = source.exclusiveMaximum
            copy.exclusiveMaximumValue = source.exclusiveMaximumValue
            copy.exclusiveMinimum = source.exclusiveMinimum
            copy.exclusiveMinimumValue = source.exclusiveMinimumValue
            copy.externalDocs = source.externalDocs
            copy.format = source.format
            copy.items = source.items
            copy.maxContains = source.maxContains
            copy.maxItems = source.maxItems
            copy.maxLength = source.maxLength
            copy.maxProperties = source.maxProperties
            copy.maximum = source.maximum
            copy.minContains = source.minContains
            copy.minItems = source.minItems
            copy.minLength = source.minLength
            copy.minProperties = source.minProperties
            copy.minimum = source.minimum
            copy.multipleOf = source.multipleOf
            copy.name = source.name
            copy.not = source.not
            copy.nullable = source.nullable
            copy.oneOf = source.oneOf
            copy.pattern = source.pattern
            copy.properties = source.properties
            copy.readOnly = source.readOnly
            copy.required = source.required
            copy.setConst(source.const)
            copy.setDefault(source.default)
            copy.specVersion = source.specVersion
            copy.title = source.title
            copy.type = source.type
            copy.uniqueItems = source.uniqueItems
            copy.writeOnly = source.writeOnly
            copy.xml = source.xml
        }
    }

}