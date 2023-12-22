package io.github.smiley4.schemakenerator.parser.core

data class TypeParameterData(
    val name: String,
    val type: TypeId,
    val nullable: Boolean
)