package io.github.smiley4.schemakenerator.core.typedata

/**
 * Data of an annotation
 */
data class AnnotationData(
    /**
     * The name of the annotation - usually the full qualified name of the annotation-class
     */
    var name: String,
    /**
     * the values of the annotation
     */
    val values: MutableMap<String, Any?>,
)
