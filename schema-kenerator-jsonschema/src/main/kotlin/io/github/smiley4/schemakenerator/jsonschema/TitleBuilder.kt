package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.TypeName


object TitleBuilder {

    val BUILDER_MINIMAL: (schema: TypeData, types: Map<TypeId, TypeData>) -> String = { type, types -> buildMinimal(type, types) }

    val BUILDER_SIMPLE: (schema: TypeData, types: Map<TypeId, TypeData>) -> String = { type, types -> buildSimple(type, types) }

    val BUILDER_FULL: (schema: TypeData, types: Map<TypeId, TypeData>) -> String = { type, types -> buildFull(type, types) }

    private fun buildMinimal(type: TypeData, types: Map<TypeId, TypeData>): String {
        return buildString {
            append(type.descriptiveName.short)
            if(type.typeParameters.isNotEmpty()) {
                append("<")
                append(type.typeParameters.joinToString(",") { buildMinimal(types[it.type]!!, types) })
                append(">")
            }
        }
    }

    private fun buildSimple(type: TypeData, types: Map<TypeId, TypeData>): String {
        return buildString {
            append(getSimpleName(type.descriptiveName))
            if(type.typeParameters.isNotEmpty()) {
                append("<")
                append(type.typeParameters.joinToString(",") { buildSimple(types[it.type]!!, types) })
                append(">")
            }
        }
    }

    private fun buildFull(type: TypeData, types: Map<TypeId, TypeData>): String {
        return buildString {
            append(type.descriptiveName.full)
            if(type.typeParameters.isNotEmpty()) {
                append("<")
                append(type.typeParameters.joinToString(",") { buildFull(types[it.type]!!, types) })
                append(">")
            }
        }
    }

    private fun getSimpleName(name: TypeName): String {
        return if (name.packageName.isNotBlank() && name.full.contains(name.packageName)) {
            // we have a valid package name provided - use that to extract the simple name
            name.full
                .replace(name.packageName, "")
                .split(".")
                .filter { it != "Companion" && it.isNotBlank() }
                .joinToString(".")
        } else {
            // we don't have a valid package name provided - try to determine simple name by looking at common patterns
            name.full
                .split(".")
                .filter { !it.toCharArray().first().isLowerCase() && it != "Companion" }
                .joinToString(".")
        }
    }


}
