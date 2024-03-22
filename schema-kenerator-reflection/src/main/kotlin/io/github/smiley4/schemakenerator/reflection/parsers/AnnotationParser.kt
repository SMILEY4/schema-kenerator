package io.github.smiley4.schemakenerator.reflection.parsers

import io.github.smiley4.schemakenerator.core.parser.AnnotationData
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

class AnnotationParser(private val typeParser: ReflectionTypeParser) {

    fun parse(clazz: KClass<*>): List<AnnotationData> {
        return clazz.annotations.map { parseAnnotation(it) }
    }

    fun parse(property: KProperty<*>): List<AnnotationData> {
        return property.annotations.map { parseAnnotation(it) }
    }

    fun parse(property: KFunction<*>): List<AnnotationData> {
        return property.annotations.map { parseAnnotation(it) }
    }

    private fun parseAnnotation(annotation: Annotation): AnnotationData {
        return AnnotationData(
            name = annotation.annotationClass.qualifiedName ?: "",
            annotation = annotation,
            values = annotation.annotationClass.members
                .filterIsInstance<KProperty<*>>()
                .associate { it.name to it.getter.call(annotation) }
                .toMutableMap()
        )
    }

}