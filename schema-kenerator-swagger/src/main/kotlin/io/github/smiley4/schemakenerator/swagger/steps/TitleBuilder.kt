package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId

object TitleBuilder {

    /**
     * Only the (short) name of the type and the type (short) names of type parameters formatted like a java/kotlin class.
     * May not result in a valid string for an openapi spec.
     * Example: MyType<MyParam1,MyParam2>
     */
    val BUILDER_SIMPLE: (schema: BaseTypeData, types: Map<TypeId, BaseTypeData>) -> String =
        { type, types -> buildSimple(type, types) }


    /**
     * Only the (full) name of the type and the type (full) names of type parameters formatted like a java/kotlin class.
     * May not result in a valid string for an openapi spec.
     * Example: my.path.MyType<my.path.MyParam1,my.path.MyParam2>
     */
    val BUILDER_FULL: (schema: BaseTypeData, types: Map<TypeId, BaseTypeData>) -> String =
        { type, types -> buildFull(type, types) }


    /**
     * Only the (short) name of the type and the type (short) names of type parameters formatted to be valid for an openapi spec.
     * Example: MyType_MyParam1-MyParam2
     */
    val BUILDER_OPENAPI_SIMPLE: (schema: BaseTypeData, types: Map<TypeId, BaseTypeData>) -> String =
        { type, types -> buildOpenApiSimple(type, types) }


    /**
     * Only the (short) name of the type and the type (short) names of type parameters formatted to be valid for an openapi spec.
     * Example: my.path.MyType_my.path.MyParam1-my.path.MyParam2
     */
    val BUILDER_OPENAPI_FULL: (schema: BaseTypeData, types: Map<TypeId, BaseTypeData>) -> String =
        { type, types -> buildOpenApiFull(type, types) }

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
                it + (type.id.additionalId?.let { a -> "#$a" }.orEmpty())
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
                it + (type.id.additionalId?.let { a -> "#$a" }.orEmpty())
            }
    }

    private fun buildOpenApiFull(type: BaseTypeData, types: Map<TypeId, BaseTypeData>): String {
        return type.qualifiedName
            .let {
                if (type.typeParameters.isNotEmpty()) {
                    val paramString = type.typeParameters
                        .map { (_, param) -> buildFull(types[param.type]!!, types) }
                        .joinToString("-")
                    "${it}_$paramString"
                } else {
                    it
                }
            }.let {
                it + (type.id.additionalId?.let { a -> "#$a" }.orEmpty())
            }
    }

    private fun buildOpenApiSimple(type: BaseTypeData, types: Map<TypeId, BaseTypeData>): String {
        return type.simpleName
            .let {
                if (type.typeParameters.isNotEmpty()) {
                    val paramString = type.typeParameters
                        .map { (_, param) -> buildSimple(types[param.type]!!, types) }
                        .joinToString("-")
                    "${it}_$paramString"
                } else {
                    it
                }
            }.let {
                it + (type.id.additionalId?.let { a -> "#$a" }.orEmpty())
            }
    }

}
