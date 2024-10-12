package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAnnotationUtils.iterateProperties

/**
 * Further customization options
 */
class JsonSchemaCustomizeStep {

    /**
     * Provide a function that is called for each type and json-schema. Can be used to manually manipulate the generated json-schema.
     */
    fun customizeTypes(
        bundle: Bundle<JsonSchema>,
        action: (typeData: BaseTypeData, typeSchema: JsonNode) -> Unit
    ): Bundle<JsonSchema> {
        return bundle.also { schema ->
            processTypes(schema.data, action)
            schema.supporting.forEach { processTypes(it, action) }
        }
    }

    private fun processTypes(schema: JsonSchema, action: (typeData: BaseTypeData, typeSchema: JsonNode) -> Unit) {
        action(schema.typeData, schema.json)
    }


    /**
     * Provide a function that is called for each property. Can be used to manually manipulate the generated json-schema.
     */
    fun customizeProperties(
        bundle: Bundle<JsonSchema>,
        action: (propertyData: PropertyData, propertySchema: JsonNode) -> Unit
    ): Bundle<JsonSchema> {
        val typeDataMap = bundle.buildTypeDataMap()
        return bundle.also { schema ->
            processProperties(schema.data, typeDataMap, action)
            schema.supporting.forEach { processProperties(it, typeDataMap, action) }
        }
    }

    private fun processProperties(
        schema: JsonSchema,
        typeDataMap: Map<TypeId, BaseTypeData>,
        action: (typeData: PropertyData, typeSchema: JsonNode) -> Unit
    ) {
        iterateProperties(schema, typeDataMap) { prop, propData, _ ->
            action(propData, prop)
        }
    }


    /**
     * Provide a function that is called for each property. Can be used to manually manipulate the generated json-schema.
     */
    fun customizeProperties(
        bundle: Bundle<JsonSchema>,
        action: (propertyData: PropertyData, propertyTypeData: BaseTypeData, propertySchema: JsonNode) -> Unit
    ): Bundle<JsonSchema> {
        val typeDataMap = bundle.buildTypeDataMap()
        return bundle.also { schema ->
            processProperties(schema.data, typeDataMap, action)
            schema.supporting.forEach { processProperties(it, typeDataMap, action) }
        }
    }

    private fun processProperties(
        schema: JsonSchema,
        typeDataMap: Map<TypeId, BaseTypeData>,
        action: (typeData: PropertyData, propertyTypeData: BaseTypeData, typeSchema: JsonNode) -> Unit
    ) {
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            action(propData, propTypeData, prop)
        }
    }

}
