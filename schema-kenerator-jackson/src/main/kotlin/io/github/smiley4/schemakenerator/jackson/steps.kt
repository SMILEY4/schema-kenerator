package io.github.smiley4.schemakenerator.jackson

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonIgnoreType
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.smiley4.schemakenerator.core.AbstractAddDiscriminatorStep
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.InputType
import io.github.smiley4.schemakenerator.core.data.KTypeInput
import io.github.smiley4.schemakenerator.core.data.TypeData
import kotlin.reflect.KType

/**
 * Finds and adds additional subtypes from jackson [JsonSubTypes]-annotation.
 * An additional step to add missing subtype-supertype relations later may be required.
 * Add this step before any type analysis.
 * @param typeProcessing processor used to get annotation data from [KType]
 * @param maxRecursionDepth how many "levels" to search for subtypes
 */
fun KType.collectJacksonSubTypes(
    typeProcessing: (type: KType) -> Bundle<TypeData>,
    maxRecursionDepth: Int = 10
): Bundle<InputType> {
    return KTypeInput(this).collectJacksonSubTypes(typeProcessing, maxRecursionDepth)
}


/**
 * Finds and adds additional subtypes from jackson [JsonSubTypes]-annotation.
 * An additional step to add missing subtype-supertype relations later may be required.
 * Add this step before any type analysis.
 * @param typeProcessing processor used to get annotation data from [KType]
 * @param maxRecursionDepth how many "levels" to search for subtypes
 */
fun InputType.collectJacksonSubTypes(
    typeProcessing: (type: KType) -> Bundle<TypeData>,
    maxRecursionDepth: Int = 10
): Bundle<InputType> {
    return JacksonSubTypeStep(
        typeProcessing = typeProcessing,
        maxRecursionDepth = maxRecursionDepth
    ).process(this)
}


/**
 *  Handles miscellaneous jackson annotations
 *  - adds support for jackson [JsonIgnore]-annotation and removes annotated members
 *  - adds support for jackson [JsonIgnoreType]-annotation and removes members of the annotated type
 *  - adds support for jackson [JsonIgnoreProperties]-annotation and removes specified members from the annotated types.
 *  - adds support for the jackson [JsonProperty]-annotation.
 *  Renames annotated members and modifies their nullability according to the specified values.
 *  Add this step after type analysis and before schema generation.
 */
fun Bundle<TypeData>.handleJacksonAnnotations(): Bundle<TypeData> {
    return this
        .let { JacksonIgnoreStep().process(this) }
        .let { JacksonIgnoreTypeStep().process(this) }
        .let { JacksonIgnorePropertiesStep().process(this) }
        .let { JacksonPropertyStep().process(this) }
}


/**
 * Handles the [JsonTypeInfo]-annotations and adds a discriminator property with the defined name and
 * annotated with a marker annotation called [AbstractAddDiscriminatorStep.MARKER_ANNOTATION_NAME].
 * Add this step after type analysis and before schema generation.
 */
fun Bundle<TypeData>.addJacksonTypeInfoDiscriminatorProperty(): Bundle<TypeData> {
    return JacksonJsonTypeInfoDiscriminatorStep().process(this)
}
