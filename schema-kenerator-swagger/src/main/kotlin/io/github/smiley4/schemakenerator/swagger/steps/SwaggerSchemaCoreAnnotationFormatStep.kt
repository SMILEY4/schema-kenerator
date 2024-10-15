package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.annotations.Format
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.iterateProperties

/**
 * Adds format values from [Format]-annotation
 */
class SwaggerSchemaCoreAnnotationFormatStep : AbstractSwaggerSchemaStep() {

    override fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        if (schema.swagger.format == null) {
            determineFormat(schema.typeData.annotations)?.also { format ->
                schema.swagger.format = format
            }
        }
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineFormat(propData.annotations + propTypeData.annotations)?.also { format ->
                prop.format = format
            }
        }
    }

    private fun determineFormat(annotations: Collection<AnnotationData>): String? {
        return annotations
            .filter { it.name == Format::class.qualifiedName }
            .map { it.values["format"] as String }
            .firstOrNull()
    }

}
