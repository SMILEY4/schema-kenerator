package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.jsonschema.data.IntermediateJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchemaData

internal class JsonSchemaCustomizeStep {

    /**
     * Provide a function that is called for each type and json-schema. Can be used to manually manipulate the generated json-schema.
     */
    fun customizeTypes(
        input: IntermediateJsonSchemaData,
        action: (typeData: TypeData, typeSchema: JsonNode) -> Unit
    ): IntermediateJsonSchemaData {
        input.entries.forEach { processTypes(it, action) }
        return input
    }

    private fun processTypes(schema: JsonSchemaData, action: (typeData: TypeData, typeSchema: JsonNode) -> Unit) {
        action(schema.typeData, schema.json)
    }


    /**
     * Provide a function that is called for each property. Can be used to manually manipulate the generated json-schema.
     */
    fun customizeProperties(
        input: IntermediateJsonSchemaData,
        action: (propertyData: MemberData, propertySchema: JsonNode) -> Unit
    ): IntermediateJsonSchemaData {
        input.entries.forEach { processProperties(it, input.typeDataById, action) }
        return input
    }

    private fun processProperties(
        schema: JsonSchemaData,
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
        input: IntermediateJsonSchemaData,
        action: (memberData: MemberData, memberTypeData: TypeData, propertySchema: JsonNode) -> Unit
    ): IntermediateJsonSchemaData {
        input.entries.forEach { processMembers(it, input.typeDataById, action) }
        return input
    }

    private fun processMembers(
        schema: JsonSchemaData,
        typeDataMap: Map<TypeId, TypeData>,
        action: (typeData: MemberData, memberTypeData: TypeData, typeSchema: JsonNode) -> Unit
    ) {
        iterateProperties(schema, typeDataMap) { prop, memberData, memberTypeData ->
            action(memberData, memberTypeData, prop)
        }
    }

}
