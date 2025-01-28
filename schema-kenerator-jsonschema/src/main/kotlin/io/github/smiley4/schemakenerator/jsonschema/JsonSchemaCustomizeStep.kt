package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaAnnotationUtils.iterateProperties

/**
 * Further customization options
 */
class JsonSchemaCustomizeStep {

    /**
     * Provide a function that is called for each type and json-schema. Can be used to manually manipulate the generated json-schema.
     */
    fun customizeTypes(
        bundle: Bundle<JsonSchema>,
        action: (typeData: TypeData, typeSchema: JsonNode) -> Unit
    ): Bundle<JsonSchema> {
        return bundle.also { schema ->
            processTypes(schema.data, action)
            schema.supporting.forEach { processTypes(it, action) }
        }
    }

    private fun processTypes(schema: JsonSchema, action: (typeData: TypeData, typeSchema: JsonNode) -> Unit) {
        action(schema.typeData, schema.json)
    }


    /**
     * Provide a function that is called for each property. Can be used to manually manipulate the generated json-schema.
     */
    fun customizeProperties(
        bundle: Bundle<JsonSchema>,
        action: (propertyData: MemberData, propertySchema: JsonNode) -> Unit
    ): Bundle<JsonSchema> {
        val typeDataMap = bundle.buildTypeDataMap()
        return bundle.also { schema ->
            processProperties(schema.data, typeDataMap, action)
            schema.supporting.forEach { processProperties(it, typeDataMap, action) }
        }
    }

    private fun processProperties(
        schema: JsonSchema,
        typeDataMap: Map<TypeId, TypeData>,
        action: (typeData: MemberData, typeSchema: JsonNode) -> Unit
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
        action: (memberData: MemberData, memberTypeData: TypeData, propertySchema: JsonNode) -> Unit
    ): Bundle<JsonSchema> {
        val typeDataMap = bundle.buildTypeDataMap()
        return bundle.also { schema ->
            processMembers(schema.data, typeDataMap, action)
            schema.supporting.forEach { processMembers(it, typeDataMap, action) }
        }
    }

    private fun processMembers(
        schema: JsonSchema,
        typeDataMap: Map<TypeId, TypeData>,
        action: (typeData: MemberData, memberTypeData: TypeData, typeSchema: JsonNode) -> Unit
    ) {
        iterateProperties(schema, typeDataMap) { prop, memberData, memberTypeData ->
            action(memberData, memberTypeData, prop)
        }
    }

}
