package io.github.smiley4.schemakenerator.jackson.swagger

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchemaData

internal class JacksonSwaggerPropertyDescriptionStep {

    fun process(input: IntermediateSwaggerSchemaData): IntermediateSwaggerSchemaData {
        input.entries.forEach { process(it, input.typeDataById) }
        return input
    }

    private fun process(schema: SwaggerSchemaData, typeDataMap: Map<TypeId, TypeData>) {
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
