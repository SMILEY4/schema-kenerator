package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId


object TitleBuilder {

    val BUILDER_SIMPLE: (schema: TypeData, types: Map<TypeId, TypeData>) -> String = { type, types -> buildSimple(type, types) }

    val BUILDER_FULL: (schema: TypeData, types: Map<TypeId, TypeData>) -> String = { type, types -> buildFull(type, types) }

    private fun buildSimple(type: TypeData, types: Map<TypeId, TypeData>): String {
        return buildString {
            append(type.descriptiveName.short)
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

}
