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

/**
 * Resolves references in prepared swagger-schemas by collecting them in the components-section and referencing them.
 * @param pathBuilder builds the path to reference the type, i.e. which "name" to use
 */
class SwaggerSchemaCompileReferenceStep(private val pathBuilder: (type: TypeData, types: Map<TypeId, TypeData>) -> String) {

    private val schemaUtils = SwaggerSchemaUtils()

    private class Context(
        /**
         * all known input swagger schemas
         */
        val knownSchemas: List<SwaggerSchema>,
        /**
         * all known input types
         */
        val knownTypeData: Map<TypeId, TypeData>,
        /**
         *  the current list of schemas in the components section. Add new ones to this list.
         */
        val components: MutableMap<String, Schema<*>>,
        /**
         * counts how often any (original) path is used in the component-section
         */
        val pathCounters: MutableMap<String, Int>,
        /**
         * already mapped reference paths for types. Add new paths to this map.
         */
        val refPathMapping: MutableMap<TypeId, String>,
    ) {
        companion object {
            fun from(bundle: Bundle<SwaggerSchema>) = Context(
                bundle.flatten(),
                bundle.buildTypeDataMap(),
                mutableMapOf(),
                mutableMapOf(),
                mutableMapOf(),
            )
        }
    }


    /**
     * Put referenced schemas into definitions and reference them
     */
    fun compile(bundle: Bundle<SwaggerSchema>): CompiledSwaggerSchema {
        val context = Context.from(bundle)

        copyTypeToTypes(context.knownSchemas)

        val root = resolveReferences(bundle.data.swagger) { refObj ->
            resolveReference(refObj, context)
        }

        handleDiscriminatorMappings(root, context.components, context.knownTypeData)

        return CompiledSwaggerSchema(
            typeData = bundle.data.typeData,
            swagger = root,
            componentSchemas = context.components
        )
    }


    /**
     * Handles a schema object referencing another schema using a temporary reference path.
     * @param refObj the object with the temporary reference path
     * @param context the current compile context with data about input schemas and type data as well as current produced information
     */
    private fun resolveReference(refObj: Schema<*>, context: Context): Schema<*> {
        val referencedSchema = context.knownSchemas.find(TypeId(refObj.`$ref`))
        return if (referencedSchema != null) {
            if (shouldReference(referencedSchema.swagger)) {
                createRefProperty(refObj, referencedSchema, context)
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
     * @param context the current compile context with data about input schemas and type data as well as current produced information
     */
    private fun createRefProperty(
        refObj: Schema<*>,
        schema: SwaggerSchema,
        context: Context,
    ): Schema<*> {
        val refPath = if (context.refPathMapping.containsKey(schema.typeData.id)) {
            context.refPathMapping[schema.typeData.id]!!
        } else {
            var newRefPath = pathBuilder(schema.typeData, context.knownTypeData)
            context.pathCounters[newRefPath] = (context.pathCounters[newRefPath] ?: 0) + 1
            if (context.components.containsKey(newRefPath)) {
                newRefPath += context.pathCounters[newRefPath]
            }
            context.refPathMapping[schema.typeData.id] = newRefPath
            context.components[newRefPath] = placeholder() // break out of infinite loops
            context.components[newRefPath] = resolveReferences(schema.swagger) { resolveReference(it, context) }
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
