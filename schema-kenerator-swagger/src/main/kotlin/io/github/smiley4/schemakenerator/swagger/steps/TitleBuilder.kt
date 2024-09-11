package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId

object TitleBuilder {

    val BUILDER_SIMPLE: (schema: BaseTypeData, types: Map<TypeId, BaseTypeData>) -> String = { type, types -> buildSimple(type, types) }

    val BUILDER_FULL: (schema: BaseTypeData, types: Map<TypeId, BaseTypeData>) -> String = { type, types -> buildFull(type, types) }

    private fun buildSimple(type: BaseTypeData, types: Map<TypeId, BaseTypeData>): String {
        return type.simpleName
            .let {
                if (type.typeParameters.isNotEmpty()) {
                    val paramString = type.typeParameters
                        .map { (_, param) -> buildSimple(types[param.type]!!, types) }
                        .joinToString(",")
                    "$it<$paramString>"
                } else {
                    it
                }
            }.let {
                it + (type.id.additionalId?.let { a -> "#$a" } ?: "")
            }
    }

    private fun buildFull(type: BaseTypeData, types: Map<TypeId, BaseTypeData>): String {
        return type.qualifiedName
            .let {
                if (type.typeParameters.isNotEmpty()) {
                    val paramString = type.typeParameters
                        .map { (_, param) -> buildFull(types[param.type]!!, types) }
                        .joinToString(",")
                    "$it<$paramString>"
                } else {
                    it
                }
            }.let {
                it + (type.id.additionalId?.let { a -> "#$a" } ?: "")
            }
    }

}