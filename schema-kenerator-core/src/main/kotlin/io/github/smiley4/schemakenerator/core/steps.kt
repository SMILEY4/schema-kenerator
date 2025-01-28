package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.annotations.Name
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeData

/**
 * Adds missing subtype-supertype relations between the given types. Types not already present in the input are not included.
 * Example:
 * B is a subtype of A. B has A in its list of supertypes, but A not B as a subtype.
 * This step finds these missing connections and adds them.
 * Add this step after type analysis and before schema generation.
 */
fun Bundle<TypeData>.addMissingSupertypeSubtypeRelations(): Bundle<TypeData> {
    return AddMissingSubtypeSupertypeRelations().process(this)
}


/**
 * Changes the [TypeData.descriptiveName] to the name specified by a [Name]-annotation.
 * Add this step after type analysis and before schema generation.
 */
fun Bundle<TypeData>.handleNameAnnotation(): Bundle<TypeData> {
    return HandleNameAnnotationStep().process(this)
}


/**
 * Renames members of types according to the given function.
 * Add this step after type analysis and before schema generation.
 */
fun Bundle<TypeData>.renameMembers(rename: (name: String) -> String): Bundle<TypeData> {
    return RenameMembersStep(rename).process(this)
}


/**
 * Merges getters with their matching property:
 *  - if a matching property exists, the getter will be removed and relevant data copied to the property
 *  - if no property exists (e.g. because it is private), the getter will be removed and a new property from its data is created
 *  Add this step after type analysis and before schema generation.
 */
fun Bundle<TypeData>.gettersToProperties(): Bundle<TypeData> {
    return GettersToPropertiesStep().process(this)
}


/**
 * Adds properties to types with subtypes used to differentiate between the possible subtypes when (de-)serializing.
 * The created property is annotated with a marker annotation with the name [AbstractAddDiscriminatorStep.MARKER_ANNOTATION_NAME].
 * If a type already contains a property annotated with the marker annotation, no new property will be added.
 * Add this step after type analysis and before schema generation.
 * @param discriminatorPropertyName the name of the property to add. The type will always be [String].
 */
fun Bundle<TypeData>.addDiscriminatorProperty(discriminatorPropertyName: String = "type"): Bundle<TypeData> {
    return AddDiscriminatorStep(discriminatorPropertyName).process(this)
}
