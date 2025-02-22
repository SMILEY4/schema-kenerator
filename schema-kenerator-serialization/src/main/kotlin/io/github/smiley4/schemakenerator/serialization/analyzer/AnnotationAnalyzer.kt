package io.github.smiley4.schemakenerator.serialization.analyzer

import io.github.smiley4.schemakenerator.core.data.AnnotationData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import java.lang.reflect.Modifier
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

/**
 * Analysis functions for annotations using reflection.
 */
class AnnotationAnalyzer {

    /**
     * Analyze the annotations on the given serial descriptor.
     * @return the list of resulting annotation data
     */
    @OptIn(ExperimentalSerializationApi::class)
    fun analyzeAnnotations(descriptor: SerialDescriptor): MutableList<AnnotationData> {
        return analyzeAnnotations(descriptor.annotations)
    }


    /**
     * Analyze the given list of annotations.
     * @return the list of resulting annotation data
     */
    fun analyzeAnnotations(annotations: List<Annotation>): MutableList<AnnotationData> {
        return unwrapAnnotations(annotations).map { analyzeAnnotation(it) }.toMutableList()
    }


    /**
     * Unwrap any "container" (i.e. "repeatable") annotation in the given list of annotations.
     * "repeatable" annotations are wrapped in a container class and need to be unwrapped
     * @return the flat list of all annotations. Each instance of a "repeatable" annotation is an item in the list.
     */
    fun unwrapAnnotations(annotations: List<Annotation>): List<Annotation> {
        return annotations.flatMap { annotation ->
            if (isAnnotationContainer(annotation)) {
                unwrapContainer(annotation)
            } else {
                listOf(annotation)
            }
        }
    }


    /**
     * @return whether the given annotation is a "container" annotation containing multiple "repeatable" annotation of the same type.
     */
    fun isAnnotationContainer(annotation: Annotation): Boolean {
        return annotation.annotationClass.java.declaredAnnotations
            .map { it.annotationClass.qualifiedName }
            .contains("kotlin.jvm.internal.RepeatableContainer")
    }


    /**
     * Unwraps the given container annotation containing multiple "repeatable" annotations of the same type.
     * @return each contained annotation as an own item in the list
     */
    @Suppress("SwallowedException")
    fun unwrapContainer(annotation: Annotation): List<Annotation> {
        try {
            // A repeatable annotation container must have a method "value" returning the array of repeated annotations.
            val valueMethod = annotation.javaClass.getMethod("value")
            @Suppress("UNCHECKED_CAST")
            return (valueMethod(annotation) as Array<Annotation>).asList()
        } catch (e: Exception) {
            return emptyList()
        }
    }


    /**
     * Analyze the given annotation
     * @return the result as [AnnotationData]
     */
    fun analyzeAnnotation(annotation: Annotation): AnnotationData {
        return AnnotationData(
            name = annotation.annotationClass.qualifiedName ?: annotation.annotationClass.java.name,
            values = annotation.annotationClass.members
                .filterIsInstance<KProperty<*>>()
                .filter { it.javaField?.let { jf -> !Modifier.isStatic(jf.modifiers) } ?: true }
                .associate { it.name to it.getter.call(annotation) }
                .toMutableMap()
        )
    }

}
