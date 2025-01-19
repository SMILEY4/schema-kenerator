package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.annotations.Type
import old.AnnotationData
import old.BaseTypeData
import old.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.iterateProperties

/**
 * Modifies type of swagger-objects with the core [Type]-annotation.
 */
class SwaggerSchemaCoreAnnotationTypeStep : AbstractSwaggerSchemaStep() {

    override fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        determineType(schema.typeData.annotations)?.also { type ->
            schema.swagger.types = setOf(type)
        }
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineType(propData.annotations + propTypeData.annotations)?.also { type ->
                prop.types = setOf(type)
            }
        }
    }

    private fun determineType(annotations: Collection<AnnotationData>): String? {
        return annotations
            .filter { it.name == Type::class.qualifiedName }
            .map { it.values["type"] as String }
            .firstOrNull()
    }

}
