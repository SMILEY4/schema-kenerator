package io.github.smiley4.schemakenerator.data

data class ParentTypeInfo(
    val qualifiedName: String,
    val generics: Map<String,TypeInformation>,
) {

    companion object {

        val UNKNOWN = ParentTypeInfo(
            qualifiedName = "?",
            generics = emptyMap()
        )

    }

}