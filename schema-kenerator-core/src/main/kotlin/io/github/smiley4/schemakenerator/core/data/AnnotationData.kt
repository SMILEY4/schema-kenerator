package io.github.smiley4.schemakenerator.core.data

/**
 * Data of an annotation
 */
data class AnnotationData(
    /**
     * The name of the annotation - usually the full qualified name of the annotation-class
     */
    val name: String,
    /**
     * the values of the annotation
     */
    val values: MutableMap<String, Any?> = mutableMapOf(),
    /**
     * the annotation itself, if available
     */
    val annotation: Annotation? = null,
)
