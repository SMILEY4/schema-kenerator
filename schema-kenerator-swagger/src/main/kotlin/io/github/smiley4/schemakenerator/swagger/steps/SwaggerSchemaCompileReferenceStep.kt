package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.flatten
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.getRefPath
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.merge
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.resolveReferences
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.shouldReference
import io.swagger.v3.oas.models.media.Schema

/**
 * Resolves references in prepared swagger-schemas by collecting them in the components-section and referencing them.
 * @param pathType how to reference the type, i.e. which name to use
 */
class SwaggerSchemaCompileReferenceStep(private val pathType: RefType = RefType.FULL) {

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
        val referencedSchema = schemaList.get(referencedId)
        return if (referencedSchema != null) {
            if (shouldReference(referencedSchema.swagger)) {
                val refPath = getRefPath(pathType, referencedSchema.typeData, typeDataMap)
                if(!components.containsKey(refPath)) {
                    components[refPath] = placeholder() // break out of infinite loops
                    components[refPath] = resolveReferences(referencedSchema.swagger) { resolve(it, schemaList, typeDataMap, components)}
                }
                schemaUtils.referenceSchema(refPath, true)
            } else {
                merge(refObj, referencedSchema.swagger)
            }
        } else {
            refObj
        }
    }

    private fun placeholder() = Schema<Any>()

    private fun Collection<SwaggerSchema>.get(id: TypeId): SwaggerSchema? {
        return this.find { it.typeData.id == id }
    }

}
