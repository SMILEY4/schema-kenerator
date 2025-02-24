package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.annotations.Format
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchemaData

internal class SwaggerSchemaCoreAnnotationFormatStep {

    fun process(input: IntermediateSwaggerSchemaData): IntermediateSwaggerSchemaData {
        input.entries.forEach { process(it, input.typeDataById) }
        return input
    }

    private fun process(schema: SwaggerSchemaData, typeDataMap: Map<TypeId, TypeData>) {
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
