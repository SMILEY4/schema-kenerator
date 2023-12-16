package io.github.smiley4.schemakenerator.analysis.data

data class TypeData(
    val simpleName: String,
    val qualifiedName: String,
    val typeParameters: Map<String, TypeParameterData>,
    val supertypes: List<TypeRef>,
    val members: List<MemberData>,
)