package io.github.smiley4.schemakenerator.core.data

/**
 * Data of a property, e.g. of a field or function
 */
data class PropertyData(
    /**
     * the name of the property
     */
    val name: String,
    /**
     * the id of the (return) type
     */
    val type: TypeId,
    /**
     * whether the (return) type is nullable
     */
    val nullable: Boolean,
    /**
     * the general visibility of this property
     */
    val visibility: Visibility,
    /**
     * the kind of property (e.g. field/property or function
     */
    val kind: PropertyType,
    /**
     * the list of annotations of this property
     */
    val annotations: List<AnnotationData> = emptyList()
)