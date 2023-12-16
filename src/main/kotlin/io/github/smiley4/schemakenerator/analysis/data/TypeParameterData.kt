package io.github.smiley4.schemakenerator.analysis.data

data class TypeParameterData(
    val name: String,
    val type: TypeRef,
    val nullable: Boolean
)