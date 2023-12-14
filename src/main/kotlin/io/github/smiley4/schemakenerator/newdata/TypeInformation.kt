package io.github.smiley4.schemakenerator.newdata

data class TypeInformation(
    val simpleName: String,
    val qualifiedName: String,
    val generics: List<GenericInformation>,
    val properties: List<PropertyInformation>,
    val superTypes: List<TypeRef>,
    val baseType: Boolean,
) {

    companion object {

        val UNKNOWN = TypeInformation(
            generics = emptyList(),
            simpleName = "?",
            qualifiedName = "?",
            properties = emptyList(),
            superTypes = emptyList(),
            baseType = false
        )


    }

}