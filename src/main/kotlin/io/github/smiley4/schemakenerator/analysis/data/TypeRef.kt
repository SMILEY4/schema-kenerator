package io.github.smiley4.schemakenerator.analysis.data

import kotlin.reflect.KClass
import kotlin.reflect.KType

data class TypeRef(
    val id: String,
) {

    companion object {

        fun wildcard() = TypeRef("*")

        fun forType(kType: KType, resolvedTypeParameters: Map<String, TypeParameterData>): TypeRef {
            return TypeRef(buildId(kType, resolvedTypeParameters))
        }

        fun buildId(kType: KType, resolvedTypeParameters: Map<String, TypeParameterData>): String {
            var id = when (val classifier = kType.classifier) {
                is KClass<*> -> classifier.qualifiedName ?: "?"
                else -> "?"
            }

            val strTypeParameters = when (val classifier = kType.classifier) {
                is KClass<*> -> {
                    classifier.typeParameters.map { typeParam ->
                        val type = resolvedTypeParameters[typeParam.name]
                        if (type != null) {
                            type.type.id + if (type.nullable) "?" else ""
                        } else {
                            "null"
                        }
                    }
                }
                else -> emptyList()
            }

            if (strTypeParameters.isNotEmpty()) {
                id += "<${strTypeParameters.joinToString(",")}>"
            }

            return id
        }

    }

}