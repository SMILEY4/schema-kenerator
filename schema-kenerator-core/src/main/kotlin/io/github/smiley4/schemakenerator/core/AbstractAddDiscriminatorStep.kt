package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.flatten
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.MemberKind
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.TypeName
import io.github.smiley4.schemakenerator.core.data.Visibility
import io.github.smiley4.schemakenerator.core.data.find
import io.github.smiley4.schemakenerator.core.data.findAnnotatedWith

/**
 * Adds properties to types with subtypes used to differentiate between the possible subtypes when (de-)serializing.
 * The created property is annotated with a marker annotation with the name [AbstractAddDiscriminatorStep.MARKER_ANNOTATION_NAME].
 * If a property with the name already exists, the marker annotation will be added to this existing property.
 * If a type already contains a property annotated with the marker annotation, no new property will be added.
 */
abstract class AbstractAddDiscriminatorStep {

    companion object {

        const val MARKER_ANNOTATION_NAME = "discriminator_marker"


        /**
         * A [TypeData] for the type of the discriminator property
         */
        val DISCRIMINATOR_TYPE = TypeData(
            id = TypeId.create(),
            identifyingName = TypeName(
                full = String::class.qualifiedName!!,
                short = String::class.simpleName!!,
            ),
            descriptiveName = TypeName(
                full = String::class.qualifiedName!!,
                short = String::class.simpleName!!,
            ),
            typeParameters = mutableListOf(),
            annotations = mutableListOf(),
            subtypes = mutableListOf(),
            supertypes = mutableListOf(),
            members = mutableListOf(),
            isInlineValue = false,
            enumData = null,
            collectionData = null,
            mapData = null
        )

    }

    fun process(input: Bundle<TypeData>): Bundle<TypeData> {
        val (bundleWithDiscriminatorType, discriminatorTypeId) = ensureDiscriminatorTypeExistence(input)
        bundleWithDiscriminatorType
            .flatten()
            .asSequence()
            .filter { it.subtypes.isNotEmpty() }
            .forEach { addDiscriminator(it, discriminatorTypeId) }
        return bundleWithDiscriminatorType
    }


    /**
     * Ensures that the bundle contains a valid type for the discriminator property. Adds one if necessary and returns it as a new bundle.
     */
    private fun ensureDiscriminatorTypeExistence(bundle: Bundle<TypeData>): Pair<Bundle<TypeData>, TypeId> {
        val discriminatorType = bundle.flatten().find { it.id == DISCRIMINATOR_TYPE.id }
        return if (discriminatorType != null) {
            bundle to discriminatorType.id
        } else {
            Bundle(
                data = bundle.data,
                supporting = bundle.supporting + listOf(DISCRIMINATOR_TYPE)
            ) to DISCRIMINATOR_TYPE.id
        }
    }

    private fun addDiscriminator(parentTypeData: TypeData, discriminatorType: TypeId) {
        val discriminatorName = getDiscriminatorPropertyName(parentTypeData)
        if (discriminatorName == null) {
            // no name provided -> don't add discriminator
            return
        }
        if (parentTypeData.members.findAnnotatedWith(MARKER_ANNOTATION_NAME) != null) {
            // a field marked as "discriminator" (by the marker annotation) already exists -> don't add another one
            return
        }

        parentTypeData.members.find(discriminatorName)?.also { member ->
            // a member with the correct name already exists -> don't add another one, just annotate with marker annotation
            member.annotations.add(buildMarkerAnnotation())
            return@addDiscriminator
        }

        // add new member marked with marker annotation
        parentTypeData.members.add(buildDiscriminatorProperty(discriminatorName, discriminatorType))
    }


    /**
     * @return a new [MemberData] for the discriminator property
     */
    private fun buildDiscriminatorProperty(name: String, type: TypeId) = MemberData(
        name = name,
        type = type,
        nullable = false,
        optional = false,
        visibility = Visibility.PUBLIC,
        kind = MemberKind.PROPERTY,
        annotations = mutableListOf(buildMarkerAnnotation())
    )


    /**
     * @return a new [AnnotationData] as a marker for the discriminator property
     */
    private fun buildMarkerAnnotation(): AnnotationData {
        return AnnotationData(
            name = MARKER_ANNOTATION_NAME,
            values = mutableMapOf()
        )
    }


    /**
     * Provides the name of the discriminator property. Return null to NOT add a discriminator property
     */
    abstract fun getDiscriminatorPropertyName(typeData: TypeData): String?

}
