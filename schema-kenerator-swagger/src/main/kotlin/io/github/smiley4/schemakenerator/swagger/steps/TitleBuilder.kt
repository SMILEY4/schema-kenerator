package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.core.typedata.TypeId


object TitleBuilder {

    /**
     * Only the (short) name of the type and the type (short) names of type parameters formatted like a java/kotlin class.
     * May not result in a valid string for an openapi spec.
     * Example: MyType<MyParam1,MyParam2>
     */
    val BUILDER_SIMPLE: (schema: TypeData, types: Map<TypeId, TypeData>) -> String =
        { type, types -> buildSimple(type, types) }


    /**
     * Only the (full) name of the type and the type (full) names of type parameters formatted like a java/kotlin class.
     * May not result in a valid string for an openapi spec.
     * Example: my.path.MyType<my.path.MyParam1,my.path.MyParam2>
     */
    val BUILDER_FULL: (schema: TypeData, types: Map<TypeId, TypeData>) -> String =
        { type, types -> buildFull(type, types) }


    /**
     * Only the (short) name of the type and the type (short) names of type parameters formatted to be valid for an openapi spec.
     * Example: MyType_MyParam1-MyParam2
     */
    val BUILDER_OPENAPI_SIMPLE: (schema: TypeData, types: Map<TypeId, TypeData>) -> String =
        { type, types -> buildOpenApiSimple(type, types) }


    /**
     * Only the (short) name of the type and the type (short) names of type parameters formatted to be valid for an openapi spec.
     * Example: my.path.MyType_my.path.MyParam1-my.path.MyParam2
     */
    val BUILDER_OPENAPI_FULL: (schema: TypeData, types: Map<TypeId, TypeData>) -> String =
        { type, types -> buildOpenApiFull(type, types) }

    private fun buildSimple(type: TypeData, types: Map<TypeId, TypeData>): String {
        return buildString { // todo: resolve collisions -> esp. with kotlinx
            append(type.descriptiveName.short)
            if (type.typeParameters.isNotEmpty()) {
                append("<")
                append(type.typeParameters.joinToString(",") { buildSimple(types[it.type]!!, types) })
                append(">")
            }
        }
    }

    private fun buildFull(type: TypeData, types: Map<TypeId, TypeData>): String {
        return buildString { // todo: resolve collisions -> esp. with kotlinx
            append(type.descriptiveName.full)
            if (type.typeParameters.isNotEmpty()) {
                append("<")
                append(type.typeParameters.joinToString(",") { buildFull(types[it.type]!!, types) })
                append(">")
            }
        }
    }

    private fun buildOpenApiSimple(type: TypeData, types: Map<TypeId, TypeData>): String {
        return buildString { // todo: resolve collisions -> esp. with kotlinx
            append(type.descriptiveName.short)
            if (type.typeParameters.isNotEmpty()) {
                append("_")
                append(type.typeParameters.joinToString("-") { buildOpenApiSimple(types[it.type]!!, types) })
            }
        }
    }

    private fun buildOpenApiFull(type: TypeData, types: Map<TypeId, TypeData>): String {
        return buildString { // todo: resolve collisions -> esp. with kotlinx
            append(type.descriptiveName.full)
            if (type.typeParameters.isNotEmpty()) {
                append("_")
                append(type.typeParameters.joinToString("-") { buildOpenApiFull(types[it.type]!!, types) })
            }
        }
    }

}
