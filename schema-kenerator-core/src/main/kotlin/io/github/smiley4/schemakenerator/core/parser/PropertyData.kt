package io.github.smiley4.schemakenerator.core.parser

data class PropertyData(
    val name: String,
    val type: TypeRef,
    val nullable: Boolean,
    val visibility: Visibility,
    val kind: PropertyType
)