package io.github.smiley4.schemakenerator.newdata

data class GenericInformation(
    val name: String,
    val localName: String,
    val typeRef: TypeRef,
    val nullable: Boolean
)