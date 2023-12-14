package io.github.smiley4.schemakenerator.newdata

data class PropertyInformation(
    val name: String,
    val typeRef: TypeRef,
    val nullable: Boolean
)