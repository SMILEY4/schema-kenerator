package io.github.smiley4.schemakenerator.analysis

import kotlin.reflect.KClass
import kotlin.reflect.KType

data class TypeRef(
    val id: String,
) {

    companion object {

        fun forType(kType: KType, typeParameters: Map<String, TypeRef>): TypeRef {
            return TypeRef(buildId(kType, typeParameters))
        }

        fun buildId(kType: KType, typeParameters: Map<String, TypeRef>): String {
            var id = when (val classifier = kType.classifier) {
                is KClass<*> -> classifier.qualifiedName ?: "?"
                else -> "?"
            }

            val strTypeParameters = kType.arguments.mapIndexed { index, _ ->
                val typeParamName = if (kType.classifier is KClass<*>) (kType.classifier as KClass<*>).typeParameters[index].name else "?"
                typeParameters[typeParamName]?.id ?: "?"
            }

            if (strTypeParameters.isNotEmpty()) {
                id += "<${strTypeParameters.joinToString(",")}>"
            }

            return id
        }

    }

}