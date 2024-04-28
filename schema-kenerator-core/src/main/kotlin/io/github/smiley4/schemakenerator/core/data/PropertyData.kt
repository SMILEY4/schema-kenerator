package io.github.smiley4.schemakenerator.core.data

data class PropertyData(
    val name: String,
    val type: TypeId,
    val nullable: Boolean,
    val visibility: Visibility,
    val kind: PropertyType,
    val annotations: List<AnnotationData> = emptyList()
)