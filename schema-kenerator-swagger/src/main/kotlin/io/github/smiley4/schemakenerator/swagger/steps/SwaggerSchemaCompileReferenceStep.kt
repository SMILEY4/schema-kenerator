package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.flatten
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.merge
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.resolveReferences
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.shouldReference
import io.swagger.v3.oas.models.media.Schema

/**
 * Resolves references in prepared swagger-schemas by collecting them in the components-section and referencing them.
 * @param pathBuilder builds the path to reference the type, i.e. which "name" to use
 */
class SwaggerSchemaCompileReferenceStep(private val pathBuilder: (type: BaseTypeData, types: Map<TypeId, BaseTypeData>) -> String) {

    private val schemaUtils = SwaggerSchemaUtils()


    /**
     * Put referenced schemas into definitions and reference them
     */
    fun compile(bundle: Bundle<SwaggerSchema>): CompiledSwaggerSchema {
        val schemaList = bundle.flatten()
        val typeDataMap = bundle.buildTypeDataMap()
        val components = mutableMapOf<String, Schema<*>>()

        val root = resolveReferences(bundle.data.swagger) { refObj ->
            resolve(refObj, schemaList, typeDataMap, components)
        }

        handleDiscriminatorMappings(root, components, typeDataMap)

        return CompiledSwaggerSchema(
            typeData = bundle.data.typeData,
            swagger = root,
            componentSchemas = components
        )
    }

    private fun resolve(
        refObj: Schema<*>,
        schemaList: List<SwaggerSchema>,
        typeDataMap: Map<TypeId, BaseTypeData>,
        components: MutableMap<String, Schema<*>>
    ): Schema<*> {
        val referencedId = TypeId.parse(refObj.`$ref`)
        val referencedSchema = schemaList.find(referencedId)
        return if (referencedSchema != null) {
            if (shouldReference(referencedSchema.swagger)) {
                val refPath = pathBuilder(referencedSchema.typeData, typeDataMap)
                if (!components.containsKey(refPath)) {
                    components[refPath] = placeholder() // break out of infinite loops
                    components[refPath] = resolveReferences(referencedSchema.swagger) { resolve(it, schemaList, typeDataMap, components) }
                }
                if (refObj.nullable == true) {
                    schemaUtils.referenceSchemaNullable(refPath, true)
                } else {
                    schemaUtils.referenceSchema(refPath, true)
                }
            } else {
                merge(refObj, referencedSchema.swagger).also {
                    if (it.nullable == true) {
                        it.nullable = null
                        it.types = setOf("null") + it.types
                    }
                    if (it.nullable == false) {
                        it.nullable = null
                    }
                }
            }
        } else {
            refObj
        }
    }

    private fun handleDiscriminatorMappings(
        root: Schema<*>,
        components: MutableMap<String, Schema<*>>,
        typeDataMap: Map<TypeId, BaseTypeData>
    ) {
        handleDiscriminatorMappings(root, typeDataMap)
        components.forEach { (_, schema) -> handleDiscriminatorMappings(schema, typeDataMap) }
    }

    private fun handleDiscriminatorMappings(swaggerSchema: Schema<*>, typeDataMap: Map<TypeId, BaseTypeData>) {
        if (swaggerSchema.discriminator?.mapping == null) {
            return
        }
        swaggerSchema.discriminator.mapping = swaggerSchema.discriminator.mapping.mapValues { (_, target) ->
            val referencedId = TypeId.parse(target)
            val referencedType = typeDataMap[referencedId]!!
            schemaUtils.componentReference(pathBuilder(referencedType, typeDataMap))
        }
    }

    private fun placeholder() = Schema<Any>()

    private fun Collection<SwaggerSchema>.find(id: TypeId): SwaggerSchema? {
        return this.find { it.typeData.id == id }
    }

}
