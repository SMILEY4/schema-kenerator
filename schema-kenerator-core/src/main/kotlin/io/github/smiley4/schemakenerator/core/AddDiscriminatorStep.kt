package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.data.TypeData

/**
 * Adds properties to types with subtypes used to differentiate between the possible subtypes when (de-)serializing.
 * The created property is annotated with a marker annotation with the name [AbstractAddDiscriminatorStep.MARKER_ANNOTATION_NAME].
 * If a type already contains a property annotated with the marker annotation, no new property will be added.
 * @param discriminatorPropertyName the name of the property to add. The type will always be [String].
 */
class AddDiscriminatorStep(private val discriminatorPropertyName: String) : AbstractAddDiscriminatorStep() {

    override fun getDiscriminatorPropertyName(typeData: TypeData): String {
        return discriminatorPropertyName
    }

}
