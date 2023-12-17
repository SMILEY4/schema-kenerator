package io.github.smiley4.schemakenerator.parser.data

data class TypeData(
    val simpleName: String,
    val qualifiedName: String,
    val typeParameters: Map<String, TypeParameterData>,
    val supertypes: List<TypeRef>,
    val members: List<MemberData>,
    val enumValues: List<String>
) {

    companion object {

        fun wildcard() = TypeData(
            simpleName = "*",
            qualifiedName = "*",
            typeParameters = emptyMap(),
            supertypes = emptyList(),
            members = emptyList(),
            enumValues = emptyList()
        )

        fun placeholder(ref: TypeRef) = TypeData(
            simpleName = ref.id,
            qualifiedName = ref.id,
            typeParameters = emptyMap(),
            supertypes = emptyList(),
            members = emptyList(),
            enumValues = emptyList()
        )

    }

}