package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.annotations.Description
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.iterateProperties

/**
 * Adds a description from the [Description]-annotation.
 */
class SwaggerSchemaCoreAnnotationDescriptionStep : AbstractSwaggerSchemaStep() {

    override fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        if (schema.swagger.description == null) {
            determineDescription(schema.typeData.annotations)?.also { description ->
                schema.swagger.description = description
            }
        }
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineDescription(propData.annotations + propTypeData.annotations)?.also { description ->
                prop.description = description
            }
        }
    }

    private fun determineDescription(annotations: Collection<AnnotationData>): String? {
        return annotations
            .filter { it.name == Description::class.qualifiedName }
            .map { it.values["description"] as String }
            .firstOrNull()
    }

}
