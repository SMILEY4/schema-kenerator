package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.flatten
import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.core.typedata.TypeId
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.copyTypeToTypes
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.merge
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.resolveReferences
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.shouldReference
import io.swagger.v3.oas.models.media.Schema
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * Resolves references in prepared swagger-schemas by collecting them in the components-section and referencing them.
 * @param pathBuilder builds the path to reference the type, i.e. which "name" to use
 */
class SwaggerSchemaCompileReferenceStep(private val pathBuilder: (type: TypeData, types: Map<TypeId, TypeData>) -> String) {

    private val schemaUtils = SwaggerSchemaUtils()


    /**
     * Put referenced schemas into definitions and reference them
     */
    fun compile(bundle: Bundle<SwaggerSchema>): CompiledSwaggerSchema {
        val knownSchemas = bundle.flatten()
        val knownTypeData = bundle.buildTypeDataMap()
        val components = mutableMapOf<String, Schema<*>>()
        val refPathMapping = mutableMapOf<TypeId, String>()

        copyTypeToTypes(knownSchemas)

        val root = resolveReferences(bundle.data.swagger) { refObj ->
            resolveReference(refObj, knownSchemas, knownTypeData, refPathMapping, components)
        }

        handleDiscriminatorMappings(root, components, knownTypeData)

        return CompiledSwaggerSchema(
            typeData = bundle.data.typeData,
            swagger = root,
            componentSchemas = components
        )
    }


    /**
     * Handles a schema object referencing another schema using a temporary reference path.
     * @param refObj the object with the temporary reference path
     * @param knownSchemas all known swagger schemas
     * @param knownTypeData all known types
     * @param refPathMapping already mapped reference paths for types. Add new paths to this map.
     * @param components the current list of schemas in the components section. Add new ones to this list.
     */
    private fun resolveReference(
        refObj: Schema<*>,
        knownSchemas: List<SwaggerSchema>,
        knownTypeData: Map<TypeId, TypeData>,
        refPathMapping: MutableMap<TypeId, String>,
        components: MutableMap<String, Schema<*>>
    ): Schema<*> {
        val referencedSchema = knownSchemas.find(TypeId(refObj.`$ref`))
        return if (referencedSchema != null) {
            if (shouldReference(referencedSchema.swagger)) {
                createRefProperty(refObj, referencedSchema, knownSchemas, knownTypeData, refPathMapping, components)
            } else {
                createInlineProperty(refObj, referencedSchema)
            }
        } else {
            refObj
        }
    }


    /**
     * Create a swagger-schema with a proper reference to replace the pending referencing schema.
     * @param refObj the object with the temporary reference path
     * @param schema the referenced schema
     * @param knownSchemas all input json schemas
     * @param knownTypeData all input type data
     * @param refPathMapping already mapped reference paths for types. Add new paths to this map.
     * @param components swagger schema components section. Adds new referenced schemas.
     */
    private fun createRefProperty(
        refObj: Schema<*>,
        schema: SwaggerSchema,
        knownSchemas: List<SwaggerSchema>,
        knownTypeData: Map<TypeId, TypeData>,
        refPathMapping: MutableMap<TypeId, String>,
        components: MutableMap<String, Schema<*>>,
    ): Schema<*> {
        val refPath = if(refPathMapping.containsKey(schema.typeData.id)) {
            refPathMapping[schema.typeData.id]!!
        } else {
            var newRefPath = pathBuilder(schema.typeData, knownTypeData)
            if(components.containsKey(newRefPath)) {
                newRefPath += Random.nextInt(100..999) // todo: generate better random suffix
            }
            refPathMapping[schema.typeData.id] = newRefPath
            components[newRefPath] = placeholder() // break out of infinite loops
            components[newRefPath] = resolveReferences(schema.swagger) { resolveReference(it, knownSchemas, knownTypeData, refPathMapping, components) }
            newRefPath
        }

        return if (refObj.nullable == true) {
            schemaUtils.referenceSchemaNullable(refPath, true)
        } else {
            schemaUtils.referenceSchema(refPath, true)
        }
    }


    /**
     * Create an inline swagger-schema to replace the pending referencing schema.
     * @param refObj the schema containing the reference
     * @param schema the schema referenced by [refObj]
     */
    private fun createInlineProperty(refObj: Schema<*>, schema: SwaggerSchema): Schema<*> {
        return merge(refObj, schema.swagger).also {
            if (it.nullable == true) {
                it.nullable = null
                it.types = setOf("null") + it.types
            }
            if (it.nullable == false) {
                it.nullable = null
            }
        }
    }


    /**
     * Replace temporary reference paths in discriminator mappings
     * @param root the root schema
     * @param components swagger schema components section. Adds new referenced schemas.
     * @param knownTypeData all input type data
     */
    private fun handleDiscriminatorMappings(
        root: Schema<*>,
        components: MutableMap<String, Schema<*>>,
        knownTypeData: Map<TypeId, TypeData>
    ) {
        handleDiscriminatorMappings(root, knownTypeData)
        components.forEach { (_, schema) -> handleDiscriminatorMappings(schema, knownTypeData) }
    }


    /**
     * Replace temporary reference paths in discriminator mappings
     * @param schema the current schema to process
     * @param knownTypeData all input type data
     */
    private fun handleDiscriminatorMappings(schema: Schema<*>, knownTypeData: Map<TypeId, TypeData>) {
        if (schema.discriminator?.mapping == null) {
            return
        }
        schema.discriminator.mapping = schema.discriminator.mapping.mapValues { (_, target) ->
            val referencedType = knownTypeData[TypeId(target)]!!
            schemaUtils.componentReference(pathBuilder(referencedType, knownTypeData))
        }
    }


    /**
     * @return a placeholder schema object
     */
    private fun placeholder() = Schema<Any>()


    /**
     * @return the [SwaggerSchema] for the given [TypeId]
     */
    private fun Collection<SwaggerSchema>.find(id: TypeId): SwaggerSchema? = this.find { it.typeData.id == id }

}
