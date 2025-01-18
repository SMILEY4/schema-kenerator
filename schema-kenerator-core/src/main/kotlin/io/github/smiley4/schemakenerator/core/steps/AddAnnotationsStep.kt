package io.github.smiley4.schemakenerator.core.steps

import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle

/**
 * Adds the given annotations to root types (i.e. [Bundle.data])
 */
class AddAnnotationsStep(private val annotations: Collection<AnnotationData>) {

    /**
     * Adds the annotations to the given [Bundle.data]
     */
    fun process(bundle: Bundle<BaseTypeData>): Bundle<BaseTypeData> {
        return bundle.also {
            it.data.annotations.addAll(annotations)
        }
    }

}
