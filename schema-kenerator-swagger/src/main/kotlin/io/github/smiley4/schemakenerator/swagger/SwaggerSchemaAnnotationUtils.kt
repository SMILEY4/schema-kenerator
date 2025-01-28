package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.swagger.v3.oas.models.media.Schema

object SwaggerSchemaAnnotationUtils {

    /**
     * Iterate over the properties of the given schema as pairs of [Schema] and [PropertyData].
     */
    fun iterateProperties(
        schema: SwaggerSchema,
        typeDataMap: Map<TypeId, TypeData>,
        action: (property: Schema<*>, memberData: MemberData, memberTypeData: TypeData) -> Unit
    ) {
        if (schema.swagger.properties != null) {
            schema.swagger.properties.forEach { (propKey, prop) ->
                schema.typeData.members.find { it.name == propKey }?.also { memberData ->
                    action(prop, memberData, typeDataMap[memberData.type]!!)
                }
            }
        }
    }


    /**
     * Iterate over the properties of the given schema as pairs of [Schema] and [MemberData] and
     * removes them if the condition returns true.
     */
    fun removePropertyIf(schema: SwaggerSchema, condition: (property: Schema<*>, memberData: MemberData) -> Boolean) {
        if (schema.swagger.properties != null) {
            val keysToRemove = mutableSetOf<String>()
            schema.swagger.properties.forEach { (propKey, prop) ->
                schema.typeData.members.find { it.name == propKey }?.also { memberData ->
                    if (condition(prop, memberData)) {
                        keysToRemove.add(propKey)
                    }
                }
            }
            keysToRemove.forEach {
                schema.swagger.properties.remove(it)
                schema.swagger.required?.remove(it)
            }
        }
    }

}
