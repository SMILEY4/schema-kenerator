package io.github.smiley4.schemakenerator.core.parser

data class TypeParameterData(
    val name: String,
    val type: TypeId,
    val nullable: Boolean
)