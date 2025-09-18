package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.MemberKind
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup
import io.github.smiley4.schemakenerator.core.data.TypeDataUtils.find
import io.github.smiley4.schemakenerator.core.data.TypeDataUtils.findAnnotatedWith
import io.github.smiley4.schemakenerator.core.data.TypeDataUtils.matches
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.TypeName
import io.github.smiley4.schemakenerator.core.data.Visibility



/**
 * Adds string properties to types with subtypes used to differentiate between the possible subtypes when (de-)serializing.
 * The created property is annotated with a marker annotation with the name [AddStringDiscriminatorStep.MARKER_ANNOTATION_NAME].
 * If a property with the name already exists, the marker annotation will be added to this existing property.
 * If a type already contains a property annotated with the marker annotation, no new property will be added.
 */
class AddStringDiscriminatorStep(private val discriminatorNameProvider: DiscriminatorNameProvider) {

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
                packageName = String::class.java.packageName
            ),
            descriptiveName = TypeName(
                full = String::class.qualifiedName!!,
                short = String::class.simpleName!!,
                packageName = String::class.java.packageName
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

    fun process(input: TypeDataGroup): TypeDataGroup {
        val (bundleWithDiscriminatorType, discriminatorTypeId) = ensureDiscriminatorTypeExistence(input)
        bundleWithDiscriminatorType
            .typeData
            .filter { it.subtypes.isNotEmpty() }
            .forEach { addDiscriminator(it, discriminatorTypeId) }
        return bundleWithDiscriminatorType
    }


    /**
     * Ensures that the bundle contains a valid type for the discriminator property. Adds one if necessary and returns it as a new bundle.
     */
    private fun ensureDiscriminatorTypeExistence(typeDataGroup: TypeDataGroup): Pair<TypeDataGroup, TypeId> {
        val discriminatorType = typeDataGroup.typeData.find {
            it.matches(
                other = DISCRIMINATOR_TYPE,
                compareId = false,
                compareIdentifyingName = true,
                compareDescriptiveName = true,
                compareTypeParameters = true,
                compareMembers = true
            )
        }
        return if (discriminatorType != null) {
            typeDataGroup to discriminatorType.id
        } else {
            TypeDataGroup(
                rootId = typeDataGroup.rootId,
                data = buildMap {
                    putAll(typeDataGroup.data)
                    put(DISCRIMINATOR_TYPE.id, DISCRIMINATOR_TYPE)
                }
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
    fun getDiscriminatorPropertyName(typeData: TypeData): String? {
        return discriminatorNameProvider.getDiscriminatorPropertyName(typeData)
    }

}
