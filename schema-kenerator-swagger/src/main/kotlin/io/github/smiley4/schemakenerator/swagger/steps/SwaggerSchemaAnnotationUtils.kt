package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.swagger.v3.oas.models.media.Schema

object SwaggerSchemaAnnotationUtils {

    /**
     * Iterate over the properties of the given schema as pairs of [Schema] and [PropertyData].
     */
    fun iterateProperties(
        schema: SwaggerSchema,
        typeDataMap: Map<TypeId, BaseTypeData>,
        action: (property: Schema<*>, propertyData: PropertyData, propertyTypeData: BaseTypeData) -> Unit
    ) {
        if (schema.typeData is ObjectTypeData && schema.swagger.properties != null) {
            schema.swagger.properties.forEach { (propKey, prop) ->
                schema.typeData.members.find { it.name == propKey }?.also { propertyData ->
                    action(prop, propertyData, typeDataMap[propertyData.type]!!)
                }
            }
        }
    }


    /**
     * Iterate over the properties of the given schema as pairs of [Schema] and [PropertyData] and
     * removes them if the condition returns true.
     */
    fun removePropertyIf(schema: SwaggerSchema, condition: (property: Schema<*>, data: PropertyData) -> Boolean) {
        if (schema.typeData is ObjectTypeData && schema.swagger.properties != null) {
            val keysToRemove = mutableSetOf<String>()
            schema.swagger.properties.forEach { (propKey, prop) ->
                schema.typeData.members.find { it.name == propKey }?.also { propertyData ->
                    if (condition(prop, propertyData)) {
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
