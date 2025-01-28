package io.github.smiley4.schemakenerator.core.data

/**
 * Data of a member of an object, e.g. a field or function
 */
data class MemberData(
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
     * whether the property is optional (i.e. when a default value is provided)
     */
    var optional: Boolean,
    /**
     * the general visibility of this property
     */
    var visibility: Visibility,
    /**
     * the kind of property (e.g. field/property or function
     */
    var kind: MemberKind,
    /**
     * the list of annotations of this property
     */
    val annotations: MutableList<AnnotationData>
)
