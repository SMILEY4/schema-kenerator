package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchemaData
import io.swagger.v3.oas.models.media.Schema

internal class SwaggerSchemaCustomizeStep {

    /**
     * Provide a function that is called for each type and swagger-schema. Can be used to manually manipulate the generated swagger-schema.
     */
    fun customizeTypes(
        input: IntermediateSwaggerSchemaData,
        action: (typeData: TypeData, typeSchema: Schema<*>) -> Unit
    ): IntermediateSwaggerSchemaData {
        input.entries.forEach { processTypes(it, action) }
        return input
    }

    private fun processTypes(schema: SwaggerSchemaData, action: (typeData: TypeData, typeSchema: Schema<*>) -> Unit) {
        action(schema.typeData, schema.swagger)
    }


    /**
     * Provide a function that is called for each property. Can be used to manually manipulate the generated swagger-schema.
     */
    fun customizeProperties(
        input: IntermediateSwaggerSchemaData,
        action: (propertyData: MemberData, propertySchema: Schema<*>) -> Unit
    ): IntermediateSwaggerSchemaData {
        input.entries.forEach { processProperties(it, input.typeDataById, action) }
        return input
    }

    private fun processProperties(
        schema: SwaggerSchemaData,
        typeDataMap: Map<TypeId, TypeData>,
        action: (typeData: MemberData, typeSchema: Schema<*>) -> Unit
    ) {
        iterateProperties(schema, typeDataMap) { prop, propData, _ ->
            action(propData, prop)
        }
    }


    /**
     * Provide a function that is called for each property. Can be used to manually manipulate the generated swagger-schema.
     */
    fun customizeProperties(
        input: IntermediateSwaggerSchemaData,
        action: (memberData: MemberData, memberTypeData: TypeData, propertySchema: Schema<*>) -> Unit
    ): IntermediateSwaggerSchemaData {
        input.entries.forEach { processProperties(it, input.typeDataById, action) }
        return input
    }

    private fun processProperties(
        schema: SwaggerSchemaData,
        typeDataMap: Map<TypeId, TypeData>,
        action: (memberData: MemberData, memberTypeData: TypeData, typeSchema: Schema<*>) -> Unit
    ) {
        iterateProperties(schema, typeDataMap) { prop, propData, propType ->
            action(propData, propType, prop)
        }
    }

}
