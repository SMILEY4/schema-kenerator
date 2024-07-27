package io.github.smiley4.schemakenerator.core.dsl

import io.github.smiley4.schemakenerator.core.data.AnnotationData

class AnnotationDataDsl {

    private val data = AnnotationData("", mutableMapOf(), null)

    /**
     * Set the name of this annotation
     */
    fun name(name: String) {
        data.name = name
    }

    /**
     * Add a new value with the given name
     */
    fun value(name: String, value: Any?) {
        data.values[name] = value
    }

    fun build() = data

}