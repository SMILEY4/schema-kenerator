package io.github.smiley4.schemakenerator.parser.core

data class PropertyData(
    val name: String,
    val type: TypeId,
    val nullable: Boolean,
    val visibility: Visibility,
    val kind: PropertyType
)