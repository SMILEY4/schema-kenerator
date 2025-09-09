package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.TypeName


object TitleBuilder {

    /**
     * Only the (short) name of the type and the (short) type names of type parameters formatted like a java/kotlin class.
     * The name for nested classes does not include the names of outer classes.
     * May not result in a valid string for an openapi spec.
     * Example: MyType<MyParam1,MyParam2>
     */
    val BUILDER_MINIMAL: (schema: TypeData, types: Map<TypeId, TypeData>) -> String =
        { type, types -> buildMinimal(type, types) }


    /**
     * Only the (short) name of the type and the (short) type names of type parameters formatted like a java/kotlin class.
     * The name for nested classes includes the names of outer classes.
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
     * Only the (short) name of the type and the (short) type names of type parameters formatted to be valid for an openapi spec.
     * The name for nested classes does not include the names of outer classes.
     * Example: MyType_MyParam1-MyParam2
     */
    val BUILDER_OPENAPI_MINIMAL: (schema: TypeData, types: Map<TypeId, TypeData>) -> String =
        { type, types -> buildOpenApiMinimal(type, types) }


    /**
     * Only the (short) name of the type and the (short) type names of type parameters formatted to be valid for an openapi spec.
     * The name for nested classes includes the names of outer classes.
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

    private fun buildMinimal(type: TypeData, types: Map<TypeId, TypeData>): String {
        return buildString {
            append(type.descriptiveName.short)
            if (type.typeParameters.isNotEmpty()) {
                append("<")
                append(type.typeParameters.joinToString(",") { buildMinimal(types[it.type]!!, types) })
                append(">")
            }
        }
    }

    private fun buildSimple(type: TypeData, types: Map<TypeId, TypeData>): String {
        return buildString {
            append(getSimpleName(type.descriptiveName))
            if (type.typeParameters.isNotEmpty()) {
                append("<")
                append(type.typeParameters.joinToString(",") { buildSimple(types[it.type]!!, types) })
                append(">")
            }
        }
    }

    private fun buildFull(type: TypeData, types: Map<TypeId, TypeData>): String {
        return buildString {
            append(type.descriptiveName.full)
            if (type.typeParameters.isNotEmpty()) {
                append("<")
                append(type.typeParameters.joinToString(",") { buildFull(types[it.type]!!, types) })
                append(">")
            }
        }
    }

    private fun buildOpenApiMinimal(type: TypeData, types: Map<TypeId, TypeData>): String {
        return buildString {
            append(type.descriptiveName.short)
            if (type.typeParameters.isNotEmpty()) {
                append("_")
                append(type.typeParameters.joinToString("-") { buildOpenApiMinimal(types[it.type]!!, types) })
            }
        }
    }

    private fun buildOpenApiSimple(type: TypeData, types: Map<TypeId, TypeData>): String {
        return buildString {
            append(getSimpleName(type.descriptiveName))
            if (type.typeParameters.isNotEmpty()) {
                append("_")
                append(type.typeParameters.joinToString("-") { buildOpenApiSimple(types[it.type]!!, types) })
            }
        }
    }

    private fun buildOpenApiFull(type: TypeData, types: Map<TypeId, TypeData>): String {
        return buildString {
            append(type.descriptiveName.full)
            if (type.typeParameters.isNotEmpty()) {
                append("_")
                append(type.typeParameters.joinToString("-") { buildOpenApiFull(types[it.type]!!, types) })
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
