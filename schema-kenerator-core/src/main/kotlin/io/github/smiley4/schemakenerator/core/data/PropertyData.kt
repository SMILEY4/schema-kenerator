package io.github.smiley4.schemakenerator.core.data

/**
 * Data of a property, i.e. member of a type
 */
data class PropertyData(
    val name: String,
    val type: TypeId,
    val nullable: Boolean,
    val visibility: Visibility,
    val kind: PropertyType,
    val annotations: List<AnnotationData> = emptyList()
)