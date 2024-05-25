package io.github.smiley4.schemakenerator.core.data

/**
 * Data of a property, e.g. of a field or function
 */
data class PropertyData(
    /**
     * the name of the property
     */
    var name: String,
    /**
     * the id of the (return) type
     */
    var type: TypeId,
    /**
     * whether the (return) type is nullable
     */
    var nullable: Boolean,
    /**
     * the general visibility of this property
     */
    var visibility: Visibility,
    /**
     * the kind of property (e.g. field/property or function
     */
    var kind: PropertyType,
    /**
     * the list of annotations of this property
     */
    val annotations: MutableList<AnnotationData> = mutableListOf()
)
