package io.github.smiley4.schemakenerator.jackson.swagger.steps

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.AbstractSwaggerSchemaStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.iterateProperties

/**
 * Adds a description to properties according to the jackson [JsonPropertyDescription]-annotation.
 */
class JacksonSwaggerPropertyDescriptionStep : AbstractSwaggerSchemaStep() {

    override fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            getDescription(propData.annotations + propTypeData.annotations)?.also { description ->
                prop.description = description
            }
        }
    }

    private fun getDescription(annotations: Collection<AnnotationData>): String? {
        return annotations
            .filter { it.name == JsonPropertyDescription::class.qualifiedName }
            .map { it.values["value"] as String }
            .firstOrNull()
    }

}
