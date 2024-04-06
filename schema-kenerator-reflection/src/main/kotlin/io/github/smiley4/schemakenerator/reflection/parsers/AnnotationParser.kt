package io.github.smiley4.schemakenerator.reflection.parsers

import io.github.smiley4.schemakenerator.core.parser.AnnotationData
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

class AnnotationParser(private val typeParser: ReflectionTypeParser) {

    fun parse(clazz: KClass<*>): List<AnnotationData> {
        return unwrapAnnotations(clazz.annotations).map { parseAnnotation(it) }
    }

    fun parse(property: KProperty<*>): List<AnnotationData> {
        return unwrapAnnotations(property.annotations).map { parseAnnotation(it) }
    }

    fun parse(property: KFunction<*>): List<AnnotationData> {
        return unwrapAnnotations(property.annotations).map { parseAnnotation(it) }
    }


    /**
     * "repeatable" annotations are wrapped in a container class and need to be unwrapped
     */
    private fun unwrapAnnotations(annotations: List<Annotation>): List<Annotation> {
        return annotations.flatMap { annotation ->
            if (isAnnotationContainer(annotation)) {
                unwrapContainer(annotation)
            } else {
                listOf(annotation)
            }
        }
    }

    private fun isAnnotationContainer(annotation: Annotation): Boolean {
        return annotation.annotationClass.java.declaredAnnotations
            .map { it.annotationClass.qualifiedName }
            .contains("kotlin.jvm.internal.RepeatableContainer")
    }

    private fun unwrapContainer(annotation: Annotation): List<Annotation> {
        try {
            // A repeatable annotation container must have a method "value" returning the array of repeated annotations.
            val valueMethod = annotation.javaClass.getMethod("value")
            @Suppress("UNCHECKED_CAST")
            return (valueMethod(annotation) as Array<Annotation>).asList()
        } catch (e: Exception) {
            return emptyList()
        }
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