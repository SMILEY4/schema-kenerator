package io.github.smiley4.schemakenerator.core.steps

import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.core.data.PropertyType
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.Visibility
import io.github.smiley4.schemakenerator.core.data.flatten

/**
 * Adds properties to types with subtypes used to differentiate between the possible subtypes when (de-)serializing.
 * The created property is annotated with a marker annotation with the name [AbstractAddDiscriminatorStep.MARKER_ANNOTATION_NAME].
 * If a property with the name already exists, the marker annotation will be added to this existing property.
 * If a type already contains a property annotated with the marker annotation, no new property will be added.
 */
abstract class AbstractAddDiscriminatorStep {

    companion object {

        /**
         * The name of the discriminator property to add
         */
        const val MARKER_ANNOTATION_NAME = "discriminator_marker"


        /**
         * Create a new [BaseTypeData] for the type of the discriminator property
         */
        fun buildDiscriminatorType() = PrimitiveTypeData(
            id = TypeId.build(String::class.qualifiedName!!),
            simpleName = String::class.simpleName!!,
            qualifiedName = String::class.qualifiedName!!,
        )

    }

    fun process(bundle: Bundle<BaseTypeData>): Bundle<BaseTypeData> {
        val (bundleWithDiscriminatorType, discriminatorType) = ensureDiscriminatorTypeExistence(bundle)
        bundleWithDiscriminatorType
            .flatten()
            .asSequence()
            .filterIsInstance<ObjectTypeData>()
            .filter { it.subtypes.isNotEmpty() }
            .forEach { handleParent(it, discriminatorType) }
        return bundleWithDiscriminatorType
    }

    private fun ensureDiscriminatorTypeExistence(bundle: Bundle<BaseTypeData>): Pair<Bundle<BaseTypeData>, TypeId> {
        val discriminatorTypeTemplate = buildDiscriminatorType()
        val discriminatorType = bundle.flatten().find { it.id == discriminatorTypeTemplate.id }
        return if (discriminatorType != null) {
            bundle to discriminatorType.id
        } else {
            Bundle(
                data = bundle.data,
                supporting = bundle.supporting + listOf(discriminatorTypeTemplate)
            ) to discriminatorTypeTemplate.id
        }
    }

    private fun handleParent(parentTypeData: ObjectTypeData, discriminatorType: TypeId) {
        val discriminatorName = getDiscriminatorPropertyName(parentTypeData)
        if(discriminatorName == null) {
            return
        }
        if (parentTypeData.members.any { it.annotations.any { a -> a.name == MARKER_ANNOTATION_NAME } }) {
            return
        }
        if (parentTypeData.members.any { it.name == discriminatorName }) {
            parentTypeData.members
                .find { it.name == discriminatorName }
                ?.also { it.annotations.add(buildMarkerAnnotation()) }
            return
        }
        parentTypeData.members.add(buildProperty(discriminatorName, discriminatorType))
    }

    private fun buildProperty(name: String, type: TypeId) = PropertyData(
        name = name,
        type = type,
        nullable = false,
        optional = false,
        visibility = Visibility.PUBLIC,
        kind = PropertyType.PROPERTY,
        annotations = mutableListOf(buildMarkerAnnotation())
    )

    private fun buildMarkerAnnotation(): AnnotationData {
        return AnnotationData(name = MARKER_ANNOTATION_NAME)
    }

    /**
     * Provides the name of the discriminator property. Return null to NOT add a discriminator property
     */
    abstract fun getDiscriminatorPropertyName(typeData: ObjectTypeData): String?

}
