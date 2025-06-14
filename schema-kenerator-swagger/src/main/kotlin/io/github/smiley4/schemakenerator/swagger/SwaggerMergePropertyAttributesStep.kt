package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchemaData
import io.swagger.v3.oas.models.media.Schema

internal class SwaggerMergePropertyAttributesStep {

    fun process(input: IntermediateSwaggerSchemaData): IntermediateSwaggerSchemaData {
        val open = input.entries.toMutableList()
        val content = input.data.toMutableMap()

        while (open.isNotEmpty()) {
            val current = open.removeFirst()
            process(current, content).forEach {
                content[it.typeData.id] = it
                open.add(it)
            }
        }

        return IntermediateSwaggerSchemaData(
            rootId = input.rootId,
            data = content
        )
    }

    private fun process(schema: SwaggerSchemaData, schemas: Map<TypeId, SwaggerSchemaData>): List<SwaggerSchemaData> {
        val resultingSchemas = mutableListOf<SwaggerSchemaData>()
        schema.swagger.properties?.values
            ?.filter { it.`$ref` != null }
            ?.forEach { property ->
                val propertyData = schemas[TypeId(property.`$ref`)]
                if (propertyData != null && needsDerivative(property, propertyData.swagger)) {
                    val derivedId = TypeId.create()
                    val derivedPropertyTypeData = propertyData.typeData.copy(derivedId)
                    val derivedPropertySchema = SwaggerSchemaCompileUtils.copy(propertyData.swagger)
                    SwaggerSchemaCompileUtils.mergeInto(property, derivedPropertySchema)
                    resultingSchemas.add(SwaggerSchemaData(derivedPropertySchema, derivedPropertyTypeData))
                    property.`raw$ref`(derivedId.id)
                }
            }
        return resultingSchemas
    }

    @Suppress("CyclomaticComplexMethod", "ReturnCount")
    private fun needsDerivative(property: Schema<*>, schema: Schema<*>): Boolean {
        if(property.additionalProperties != null) return true
        if(property.allOf != null) return true
        if(property.anyOf != null) return true
        if(property.contains != null) return true
        if(property.deprecated != null && property.deprecated != schema.deprecated) return true
        if(property.discriminator != null) return true
        if(property.enum != null && property.enum != schema.enum) return true
        if(property.example != null && property.example != schema.example) return true
        if(property.examples != null && property.examples != schema.examples) return true
        if(property.exclusiveMaximum != null && property.exclusiveMaximum != schema.exclusiveMaximum) return true
        if(property.exclusiveMaximumValue != null && property.exclusiveMaximumValue != schema.exclusiveMaximumValue) return true
        if(property.exclusiveMinimum != null && property.exclusiveMinimum != schema.exclusiveMinimum) return true
        if(property.exclusiveMinimumValue != null && property.exclusiveMinimumValue != schema.exclusiveMinimumValue) return true
        if(property.externalDocs != null) return true
        if(schema.format != null && property.format != schema.format) return true
        if(property.items != null) return true
        if(property.maxContains != null && property.maxContains != schema.maxContains) return true
        if(property.maxItems != null && property.maxItems != schema.maxItems) return true
        if(property.maxLength != null && property.maxLength != schema.maxLength) return true
        if(property.maxProperties != null && property.maxProperties != schema.maxProperties) return true
        if(property.maximum != null && property.maximum != schema.maximum) return true
        if(property.minContains != null && property.minContains != schema.minContains) return true
        if(property.minItems != null && property.minItems != schema.minItems) return true
        if(property.minLength != null && property.minLength != schema.minLength) return true
        if(property.minProperties != null && property.minProperties != schema.minProperties) return true
        if(property.minimum != null && property.minimum != schema.minimum) return true
        if(property.multipleOf != null && property.multipleOf != schema.multipleOf) return true
        if(property.name != null && property.name != schema.name) return true
        if(property.not != null) return true
        if(property.oneOf != null) return true
        if(property.pattern != null && property.pattern != schema.pattern) return true
        if(property.properties != null) return true
        if(property.readOnly != null && property.readOnly != schema.readOnly) return true
        if(property.required != null && property.required != schema.required) return true
        if(property.const != null && property.const != schema.const) return true
        if(property.default != null && property.default != schema.default) return true
        if(property.title != null && property.title != schema.title) return true
        if(property.type != null && property.type != schema.type) return true
        if(property.types != null && property.types != schema.types) return true
        if(property.uniqueItems != null && property.uniqueItems != schema.uniqueItems) return true
        if(property.writeOnly != null && property.writeOnly != schema.writeOnly) return true
        return false
    }

}
