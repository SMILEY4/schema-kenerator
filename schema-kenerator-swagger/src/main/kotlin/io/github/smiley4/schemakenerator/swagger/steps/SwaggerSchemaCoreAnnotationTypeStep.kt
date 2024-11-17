package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.annotations.Type
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.iterateProperties

/**
 * Modifies type of swagger-objects with the core [Type]-annotation.
 */
class SwaggerSchemaCoreAnnotationTypeStep : AbstractSwaggerSchemaStep() {

    override fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        if (schema.swagger.format == null) {
            determineType(schema.typeData.annotations)?.also { format ->
                schema.swagger.types = setOf(format)
            }
        }
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineType(propData.annotations + propTypeData.annotations)?.also { format ->
                prop.types = setOf(format)
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
