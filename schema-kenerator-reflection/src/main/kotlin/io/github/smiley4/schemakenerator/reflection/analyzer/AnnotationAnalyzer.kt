package io.github.smiley4.schemakenerator.reflection.analyzer

import io.github.smiley4.schemakenerator.core.data.AnnotationData
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

/**
 * Analysis functions for annotations using reflection.
 */
class AnnotationAnalyzer {

    /**
     * Analyze annotations on the given class.
     * @param clazz the class to analyze
     * @return the list of analyzed annotations
     */
    fun analyzeAnnotations(clazz: KClass<*>): List<AnnotationData> {
        return unwrapAnnotations(clazz.annotations).map { analyzeAnnotation(it) }
    }

    /**
     * Analyze annotations on the given property.
     * @param property the property to analyze
     * @return the list of analyzed annotations
     */
    fun analyzeAnnotations(property: KFunction<*>): List<AnnotationData> {
        return unwrapAnnotations(property.annotations).map { analyzeAnnotation(it) }
    }

    /**
     * Analyze annotations on the given property including annotations on matching (primary) constructor parameters.
     * @param property the property to analyze
     * @param clazz the class with the constructor to analyze
     * @return the list of analyzed annotations
     */
    fun analyzeAnnotations(property: KProperty<*>, clazz: KClass<*>): List<AnnotationData> {
        return buildList {
            addAll(unwrapAnnotations(property.javaField?.annotations?.toList() ?: emptyList()).map { analyzeAnnotation(it) })
            addAll(unwrapAnnotations(property.annotations).map { analyzeAnnotation(it) })
            clazz.primaryConstructor?.parameters
                ?.find { it.name == property.name && it.type == property.returnType }?.annotations
                ?.also { annotations -> addAll(unwrapAnnotations(annotations).map { analyzeAnnotation(it) }) }
        }
    }


    /**
     * Unwrap any "container" (i.e. "repeatable") annotation in the given list of annotations.
     * "repeatable" annotations are wrapped in a container class and need to be unwrapped.
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
     * Analyzes the given annotation.
     * @return the result as [AnnotationData]
     */
    fun analyzeAnnotation(annotation: Annotation): AnnotationData {
        if (isAnnotationContainer(annotation)) {
            throw IllegalArgumentException("Annotation is an annotation container. Unwrap annotation first.")
        }
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
