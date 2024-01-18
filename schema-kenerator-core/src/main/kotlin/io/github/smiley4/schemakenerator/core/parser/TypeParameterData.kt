package io.github.smiley4.schemakenerator.core.parser

data class TypeParameterData(
    val name: String,
    val type: TypeRef,
    val nullable: Boolean
)