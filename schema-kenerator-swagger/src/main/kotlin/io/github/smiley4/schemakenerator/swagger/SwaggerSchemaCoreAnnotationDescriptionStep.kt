package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.annotations.Description
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchemaData

internal class SwaggerSchemaCoreAnnotationDescriptionStep {

    fun process(input: IntermediateSwaggerSchemaData): IntermediateSwaggerSchemaData {
        input.entries.forEach { process(it, input.typeDataById) }
        return input
    }

    private fun process(schema: SwaggerSchemaData, typeDataMap: Map<TypeId, TypeData>) {
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
