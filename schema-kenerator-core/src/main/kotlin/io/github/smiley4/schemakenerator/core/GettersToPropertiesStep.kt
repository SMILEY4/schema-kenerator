package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.MemberKind
import io.github.smiley4.schemakenerator.core.data.TypeData

internal class GettersToPropertiesStep {

    fun process(input: Bundle<TypeData>): Bundle<TypeData> {
        return input.also { data ->
            process(data.data)
            data.supporting.forEach { process(it) }
        }
    }

    private fun process(input: TypeData) {

        val toAdd = mutableListOf<MemberData>()

        input.members
            .filter { it.kind == MemberKind.GETTER }
            .forEach { getter ->

                // find matching property
                val propertyName = getterNameToPropertyName(getter.name)
                val property = input.members
                    .filter { it.kind == MemberKind.PROPERTY }
                    .find { it.name == propertyName && it.type == getter.type }

                if (property != null) {
                    // property already exists -> copy some information from getter to property
                    property.annotations.addAll(getter.annotations)
                    property.nullable = getter.nullable
                    property.visibility = getter.visibility
                } else {
                    // property does not exist yet -> create new property from getter
                    toAdd.add(
                        MemberData(
                            name = propertyName,
                            type = getter.type,
                            nullable = getter.nullable,
                            optional = getter.optional,
                            visibility = getter.visibility,
                            kind = MemberKind.PROPERTY,
                            annotations = getter.annotations
                        )
                    )
                }

            }

        // remove all getters, add created members
        input.members.removeIf { it.kind == MemberKind.GETTER }
        input.members.addAll(toAdd)
    }

    /**
     * @return the given name of a getter-function converted to a valid property name
     */
    private fun getterNameToPropertyName(name: String): String {
        if (name.startsWith("get")) {
            return name.substring("get".length).replaceFirstChar { it.lowercase() }
        }
        if (name.startsWith("is")) {
            return name.substring("is".length).replaceFirstChar { it.lowercase() }
        }
        return name
    }

}
