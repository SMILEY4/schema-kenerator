package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.annotations.Default
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.iterateProperties

/**
 * Adds default values from [Default]-annotation
 */
class SwaggerSchemaCoreAnnotationDefaultStep : AbstractSwaggerSchemaStep() {

    override fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        if (schema.swagger.default == null) {
            determineDefault(schema.typeData.annotations)?.also { default ->
                schema.swagger.setDefault(default)
            }
        }
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineDefault(propData.annotations + propTypeData.annotations)?.also { default ->
                prop.setDefault(default)
            }
        }
    }

    private fun determineDefault(annotations: Collection<AnnotationData>): String? {
        return annotations
            .filter { it.name == Default::class.qualifiedName }
            .map { it.values["value"] as String }
            .firstOrNull()
    }

}
