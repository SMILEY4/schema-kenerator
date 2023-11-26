package io.github.smiley4.schemakenerator.data

import io.github.smiley4.schemakenerator.getKType
import kotlin.reflect.KType

data class TypeInformation(
    val type: KType,
    val generics: Map<String,TypeInformation>,
    val simpleName: String,
    val qualifiedName: String,
    val properties: List<PropertyInformation>,
    val nullable: Boolean
) {

    companion object {

        val UNKNOWN = TypeInformation(
            type = getKType<TypeInformation>(),
            generics = emptyMap(),
            simpleName = "?",
            qualifiedName = "?",
            properties = emptyList(),
            nullable = false
        )

    }

}