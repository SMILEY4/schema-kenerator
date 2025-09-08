package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.EnumData
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.MemberKind
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup
import io.github.smiley4.schemakenerator.core.data.TypeDataUtils.find
import io.github.smiley4.schemakenerator.core.data.TypeDataUtils.findAnnotatedWith
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.TypeName
import io.github.smiley4.schemakenerator.core.data.Visibility

/**
 * Adds properties to types with subtypes used to differentiate between the possible subtypes when (de-)serializing.
 * The created property is annotated with a marker annotation with the name [AbstractAddDiscriminatorStep.MARKER_ANNOTATION_NAME].
 * If a property with the name already exists, the marker annotation will be added to this existing property.
 * If a type already contains a property annotated with the marker annotation, no new property will be added.
 */
abstract class AbstractAddDiscriminatorStep {

    companion object {
        const val MARKER_ANNOTATION_NAME = "discriminator_marker"
    }

    fun process(input: TypeDataGroup): TypeDataGroup {
        var typeDataGroup = input
        input.typeData
            .filter { it.subtypes.isNotEmpty() }
            .forEach { typeData ->
                addDiscriminator(typeData)?.also {
                    typeDataGroup = TypeDataGroup(
                        rootId = typeDataGroup.rootId,
                        data = buildMap {
                            putAll(typeDataGroup.data)
                            this[it.id] = it
                        }
                    )
                }
            }
        return typeDataGroup
    }

    private fun addDiscriminator(parentTypeData: TypeData): TypeData? {
        val discriminatorName = getDiscriminatorPropertyName(parentTypeData)
        if (discriminatorName == null) {
            // no name provided -> don't add discriminator
            return null
        }
        if (parentTypeData.members.findAnnotatedWith(MARKER_ANNOTATION_NAME) != null) {
            // a field marked as "discriminator" (by the marker annotation) already exists -> don't add another one
            return null
        }

        parentTypeData.members.find(discriminatorName)?.also { member ->
            // a member with the correct name already exists -> don't add another one, just annotate with marker annotation
            member.annotations.add(buildMarkerAnnotation())
            return@addDiscriminator null
        }

        // add new member marked with marker annotation
        val (discriminatorMemberData, discriminatorTypeData) = buildDiscriminatorStringProperty(discriminatorName)
        parentTypeData.members.add(discriminatorMemberData)
        return discriminatorTypeData
    }


    /**
     * @return a new [MemberData] for the discriminator property
     */
    private fun buildDiscriminatorStringProperty(name: String): Pair<MemberData, TypeData> {
        val typeData = TypeData(
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
        val memberData = MemberData(
            name = name,
            type = typeData.id,
            nullable = false,
            optional = false,
            visibility = Visibility.PUBLIC,
            kind = MemberKind.PROPERTY,
            annotations = mutableListOf(buildMarkerAnnotation())
        )
        return memberData to typeData
    }


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
