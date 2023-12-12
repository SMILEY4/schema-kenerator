package io.github.smiley4.schemakenerator.data

import io.github.smiley4.schemakenerator.getKType
import kotlin.reflect.KType

data class TypeInformation(
    val type: KType,
    val generics: Map<String,TypeInformation>,
    val simpleName: String,
    val qualifiedName: String,
    val properties: List<PropertyInformation>,
    val superTypes: List<TypeInformation>,
    val nullable: Boolean,
    val baseType: Boolean,
) {

    companion object {

        val UNKNOWN = TypeInformation(
            type = getKType<TypeInformation>(),
            generics = emptyMap(),
            simpleName = "?",
            qualifiedName = "?",
            properties = emptyList(),
            superTypes = emptyList(),
            nullable = false,
            baseType = false
        )

        fun TypeInformation.collectProperties(): List<PropertyInformation> {
            return this.properties + superTypes.flatMap { it.collectProperties() }
        }

    }

}