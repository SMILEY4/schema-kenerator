package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.AddStringDiscriminatorStep.Companion.MARKER_ANNOTATION_NAME
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
 * Adds enum properties to types with subtypes used to differentiate between the possible subtypes when (de-)serializing.
 * The created property is annotated with a marker annotation with the name [AddStringDiscriminatorStep.MARKER_ANNOTATION_NAME].
 * If a property with the name already exists, the marker annotation will be added to this existing property.
 * If a type already contains a property annotated with the marker annotation, no new property will be added.
 */
class AddEnumDiscriminatorStep(private val discriminatorNameProvider: DiscriminatorNameProvider) {

    fun process(input: TypeDataGroup): TypeDataGroup {
        val collectedTypeData = input.typeData.associateBy { it.id }.toMutableMap()
        input
            .typeData
            .filter { it.subtypes.isNotEmpty() }
            .forEach { supertype ->
                collectSubtypesRecursive(supertype, input)
                    .forEach { addDiscriminator(supertype, it, collectedTypeData) }
            }
        return TypeDataGroup(
            rootId = input.rootId,
            data = collectedTypeData,
        )
    }

    private fun collectSubtypesRecursive(type: TypeData, group: TypeDataGroup): List<TypeData> {
        return type.subtypes
            .map { group[it]!! }
            .flatMap { collectSubtypesRecursive(it, group) } + type
    }

    private fun addDiscriminator(superTypeData: TypeData, typeData: TypeData, collectedTypeData: MutableMap<TypeId, TypeData>) {
        val discriminatorName = getDiscriminatorPropertyName(superTypeData)
        if (discriminatorName == null) {
            // no name provided -> don't add discriminator
            return
        }
        if (typeData.members.findAnnotatedWith(MARKER_ANNOTATION_NAME) != null) {
            // a field marked as "discriminator" (by the marker annotation) already exists -> don't add another one
            return
        }

        typeData.members.find(discriminatorName)?.also { member ->
            // a member with the correct name already exists -> don't add another one, just annotate with marker annotation
            member.annotations.add(buildMarkerAnnotation())
            return@addDiscriminator
        }

        // add new discriminator type and member marked with marker annotation
        val (discriminatorProperty, discriminatorType) = buildDiscriminatorProperty(discriminatorName, typeData)
        collectedTypeData[discriminatorType.id] = discriminatorType
        typeData.members.add(discriminatorProperty)
    }


    /**
     * @return a new [MemberData] for the discriminator property
     */
    private fun buildDiscriminatorProperty(name: String, type: TypeData): Pair<MemberData, TypeData> {
        val typeData = TypeData(
            id = TypeId.create(),
            identifyingName = TypeName(
                full = type.identifyingName.full + "_discriminator",
                short = type.identifyingName.short + "_discriminator",
                packageName = type.identifyingName.packageName,
            ),
            descriptiveName = TypeName(
                full = type.descriptiveName.full + "_discriminator",
                short = type.descriptiveName.short + "_discriminator",
                packageName = type.descriptiveName.packageName,
            ),
            typeParameters = mutableListOf(),
            annotations = mutableListOf(),
            subtypes = mutableListOf(),
            supertypes = mutableListOf(),
            members = mutableListOf(),
            isInlineValue = false,
            enumData = EnumData(
                constants = listOf(type.identifyingName.full).toMutableList()
            ),
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
    fun getDiscriminatorPropertyName(typeData: TypeData): String? {
        return discriminatorNameProvider.getDiscriminatorPropertyName(typeData)
    }

}
