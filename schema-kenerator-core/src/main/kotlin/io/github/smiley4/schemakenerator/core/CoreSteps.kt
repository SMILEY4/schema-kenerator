package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.annotations.Name
import io.github.smiley4.schemakenerator.core.data.InitialKTypeData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup
import kotlin.reflect.KType
import kotlin.reflect.typeOf

object CoreSteps {

    /**
     * Create a new [InitialKTypeData] for the given type.
     * @param T the initial (root) type
     * @param associatedTypes list of additional type associated with the given root type
     */
    inline fun <reified T> initial(associatedTypes: List<KType> = emptyList()) = InitialKTypeData(
        type = typeOf<T>(),
        associatedTypes = associatedTypes
    )


    /**
     * Create a new [InitialKTypeData] for the given type.
     * @param type the initial (root) type
     * @param associatedTypes list of additional type associated with the given root type
     */
    fun initial(type: KType, associatedTypes: List<KType> = emptyList()) = InitialKTypeData(
        type = type,
        associatedTypes = associatedTypes
    )


    /**
     * Adds missing subtype-supertype relations between the given types. Types not already present in the input are not included.
     * Example:
     * B is a subtype of A. B has A in its list of supertypes, but A not B as a subtype.
     * This step finds these missing connections and adds them.
     * Add this step after type analysis and before schema generation.
     */
    fun TypeDataGroup.addMissingSupertypeSubtypeRelations(): TypeDataGroup {
        return AddMissingSubtypeSupertypeRelations().process(this)
    }


    /**
     * Changes the [TypeData.descriptiveName] to the name specified by a [Name]-annotation.
     * Add this step after type analysis and before schema generation.
     */
    fun TypeDataGroup.handleNameAnnotation(): TypeDataGroup {
        return HandleNameAnnotationStep().process(this)
    }


    /**
     * Renames members of types according to the given function.
     * Add this step after type analysis and before schema generation.
     */
    fun TypeDataGroup.renameMembers(rename: (name: String) -> String): TypeDataGroup {
        return RenameMembersStep(rename).process(this)
    }


    /**
     * Merges getters with their matching property:
     *  - if a matching property exists, the getter will be removed and relevant data copied to the property
     *  - if no property exists (e.g. because it is private), the getter will be removed and a new property from its data is created
     * Add this step after type analysis and before schema generation.
     */
    fun TypeDataGroup.gettersToProperties(): TypeDataGroup {
        return GettersToPropertiesStep().process(this)
    }


    /**
     * Adds properties to types with subtypes used to differentiate between the possible subtypes when (de-)serializing.
     * The created property is annotated with a marker annotation with the name [AddStringDiscriminatorStep.MARKER_ANNOTATION_NAME].
     * If a type already contains a property annotated with the marker annotation, no new property will be added.
     * Add this step after type analysis and before schema generation.
     * @param discriminatorPropertyName the name of the property to add. The type will always be [String].
     * @param asEnum whether the property should be a simple string or an enum with the (full identifying) name of the type as only option.
     */
    fun TypeDataGroup.addDiscriminatorProperty(discriminatorPropertyName: String = "type", asEnum: Boolean = false): TypeDataGroup {
        return when(asEnum) {
            true -> AddEnumDiscriminatorStep(ConstDiscriminatorNameProvider(discriminatorPropertyName)).process(this)
            false -> AddStringDiscriminatorStep(ConstDiscriminatorNameProvider(discriminatorPropertyName)).process(this)
        }
    }

}
