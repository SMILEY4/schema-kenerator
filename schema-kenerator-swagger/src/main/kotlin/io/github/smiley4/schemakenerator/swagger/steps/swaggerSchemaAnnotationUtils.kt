package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.swagger.v3.oas.models.media.Schema

fun iterateProperties(schema: SwaggerSchema, action: (property: Schema<*>, data: PropertyData) -> Unit) {
    if (schema.typeData is ObjectTypeData && schema.swagger.properties != null) {
        schema.swagger.properties.forEach { (propKey, prop) ->
            schema.typeData.members.find { it.name == propKey }?.also { propertyData ->
                action(prop, propertyData)
            }
        }
    }
}