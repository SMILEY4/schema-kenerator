package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.annotations.Default
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchemaData

internal class SwaggerSchemaCoreAnnotationDefaultStep {

    fun process(input: IntermediateSwaggerSchemaData): IntermediateSwaggerSchemaData {
        input.entries.forEach { process(it, input.typeDataById) }
        return input
    }

    private fun process(schema: SwaggerSchemaData, typeDataMap: Map<TypeId, TypeData>) {
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
