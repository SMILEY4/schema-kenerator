package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.annotations.MaxLength
import io.github.smiley4.schemakenerator.core.annotations.MinLength
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchemaData

internal class SwaggerSchemaCoreAnnotationMinMaxLengthStep {

    fun process(input: IntermediateSwaggerSchemaData): IntermediateSwaggerSchemaData {
        input.entries.forEach { process(it, input.typeDataById) }
        return input
    }

    private fun process(schema: SwaggerSchemaData, typeDataMap: Map<TypeId, TypeData>) {
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineMinLength(propData.annotations + propTypeData.annotations)?.also { value ->
                if (propTypeData.isCollection) {
                    prop.minItems = value.toInt()
                } else {
                    prop.minLength = value.toInt()
                }
            }
        }
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineMaxLength(propData.annotations + propTypeData.annotations)?.also { value ->
                if (propTypeData.isCollection) {
                    prop.maxItems = value.toInt()
                } else {
                    prop.maxLength = value.toInt()
                }
            }
        }

    }

    private fun determineMinLength(annotations: List<AnnotationData>): Long? {
        return annotations
            .filter { it.name == MinLength::class.qualifiedName }
            .map { it.values["value"] as Long }
            .firstOrNull()
    }

    private fun determineMaxLength(annotations: List<AnnotationData>): Long? {
        return annotations
            .filter { it.name == MaxLength::class.qualifiedName }
            .map { it.values["value"] as Long }
            .firstOrNull()
    }

}
