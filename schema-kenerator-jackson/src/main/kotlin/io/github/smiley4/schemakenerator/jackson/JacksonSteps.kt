package io.github.smiley4.schemakenerator.jackson

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonIgnoreType
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.smiley4.schemakenerator.core.AddEnumDiscriminatorStep
import io.github.smiley4.schemakenerator.core.AddStringDiscriminatorStep
import io.github.smiley4.schemakenerator.core.data.InitialKTypeData
import io.github.smiley4.schemakenerator.core.data.InitialTypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup
import kotlin.reflect.KType

object JacksonSteps {

    /**
     * Finds and adds additional subtypes from jackson [JsonSubTypes]-annotation.
     * An additional step to add missing subtype-supertype relations later may be required.
     * Add this step before any type analysis.
     * @param typeProcessing processor used to get annotation data from [KType]
     * @param maxRecursionDepth how many "levels" to search for subtypes
     */
    fun InitialTypeData.collectJacksonSubTypes(
        typeProcessing: (type: InitialKTypeData) -> TypeDataGroup,
        maxRecursionDepth: Int = 10
    ): InitialKTypeData {
        return when(this) {
            is InitialKTypeData -> {
                JacksonSubTypeStep(
                    typeProcessing = typeProcessing,
                    maxRecursionDepth = maxRecursionDepth
                ).process(this)
            }
            else -> throw IllegalArgumentException("Initial type data '${this::class.simpleName}' is not supported by this step.'")
        }
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
    fun TypeDataGroup.handleJacksonAnnotations(): TypeDataGroup {
        return this
            .let { JacksonIgnoreStep().process(it) }
            .let { JacksonIgnoreTypeStep().process(it) }
            .let { JacksonIgnorePropertiesStep().process(it) }
            .let { JacksonPropertyStep().process(it) }
    }


    /**
     * Handles the [JsonTypeInfo]-annotations and adds a discriminator property with the defined name and
     * annotated with a marker annotation called [AddStringDiscriminatorStep.MARKER_ANNOTATION_NAME].
     * Add this step after type analysis and before schema generation.
     * @param asEnum whether the property should be a simple string or an enum with the (full identifying) name of the type as only option.
     */
    fun TypeDataGroup.addJacksonTypeInfoDiscriminatorProperty(asEnum: Boolean = false): TypeDataGroup {
        return when(asEnum) {
            true -> AddEnumDiscriminatorStep(JacksonJsonTypeInfoDiscriminatorNameProvider()).process(this)
            false -> AddStringDiscriminatorStep(JacksonJsonTypeInfoDiscriminatorNameProvider()).process(this)
        }
    }

}
