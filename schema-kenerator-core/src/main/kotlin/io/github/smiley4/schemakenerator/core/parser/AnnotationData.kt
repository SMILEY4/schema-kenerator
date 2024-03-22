package io.github.smiley4.schemakenerator.core.parser

data class AnnotationData(
    val name: String,
    val values: MutableMap<String, Any?> = mutableMapOf(),
    val annotation: Annotation,
)