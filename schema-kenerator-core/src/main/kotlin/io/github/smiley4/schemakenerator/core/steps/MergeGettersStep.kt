package io.github.smiley4.schemakenerator.core.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.core.data.PropertyType

/**
 * Merges getters with their matching property;
 *  - if a matching property exists, the getter will be removed and relevant data copied to the property
 *  - if no property exists (e.g. because it is private), the getter will be removed and a new property from its data is created
 */
class MergeGettersStep {

    fun process(bundle: Bundle<BaseTypeData>): Bundle<BaseTypeData> {
        return bundle.also { data ->
            process(data.data)
            data.supporting.forEach { process(it) }
        }
    }

    private fun process(typeData: BaseTypeData) {
        when (typeData) {
            is ObjectTypeData -> process(typeData)
            else -> Unit
        }
    }

    private fun process(typeData: ObjectTypeData) {

        val toAdd = mutableListOf<PropertyData>()

        typeData.members
            .filter { it.kind == PropertyType.GETTER }
            .forEach { getter ->

                // find matching property
                val propertyName = getterNameToPropertyName(getter.name)
                val property = typeData.members
                    .filter { it.kind == PropertyType.PROPERTY }
                    .find { it.name == propertyName && it.type == getter.type }

                if(property != null) {
                    // copy some information from getter to property
                    property.annotations.addAll(getter.annotations)
                    property.nullable = getter.nullable
                    property.visibility = getter.visibility
                } else {
                    // create new property from getter
                    toAdd.add(PropertyData(
                        name = propertyName,
                        type = getter.type,
                        nullable = getter.nullable,
                        optional = getter.optional,
                        visibility = getter.visibility,
                        kind = PropertyType.PROPERTY,
                        annotations = getter.annotations
                    ))
                }

            }

        // remove all getters, add created members
        typeData.members.removeIf { it.kind == PropertyType.GETTER }
        typeData.members.addAll(toAdd)
    }

    private fun getterNameToPropertyName(name: String): String {
        if(name.startsWith("get")) {
            return name.substring("get".length).replaceFirstChar { it.lowercase() }
        }
        if(name.startsWith("is")) {
            return name.substring("is".length).replaceFirstChar { it.lowercase() }
        }
        return name
    }

}
