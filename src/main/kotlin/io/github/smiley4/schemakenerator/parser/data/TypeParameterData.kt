package io.github.smiley4.schemakenerator.parser.data

data class TypeParameterData(
    val name: String,
    val type: TypeRef,
    val nullable: Boolean
)