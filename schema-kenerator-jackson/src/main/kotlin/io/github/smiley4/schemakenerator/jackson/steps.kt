package io.github.smiley4.schemakenerator.jackson

import old.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.InputType
import io.github.smiley4.schemakenerator.core.data.KTypeInput
import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.jackson.steps.JacksonIgnorePropertiesStep
import io.github.smiley4.schemakenerator.jackson.steps.JacksonIgnoreStep
import io.github.smiley4.schemakenerator.jackson.steps.JacksonIgnoreTypeStep
import io.github.smiley4.schemakenerator.jackson.steps.JacksonJsonTypeInfoDiscriminatorStep
import io.github.smiley4.schemakenerator.jackson.steps.JacksonPropertyStep
import io.github.smiley4.schemakenerator.jackson.steps.JacksonSubTypeStep
import kotlin.reflect.KType

/**
 * Handles the jackson "JsonSubTypes"-annotation.
 * See [JacksonSubTypeStep] for more info.
 */
fun KType.collectJacksonSubTypes(typeProcessing: (type: KType) -> Bundle<BaseTypeData>, maxRecursionDepth: Int = 10): Bundle<InputType> {
    return KTypeInput(this).collectJacksonSubTypes(typeProcessing, maxRecursionDepth)
}

/**
 * Handles the jackson "JsonSubTypes"-annotation.
 * See [JacksonSubTypeStep] for more info.
 */
fun InputType.collectJacksonSubTypes(
    typeProcessing: (type: KType) -> Bundle<BaseTypeData>,
    maxRecursionDepth: Int = 10
): Bundle<InputType> {
    return JacksonSubTypeStep(
        typeProcessing = typeProcessing,
        maxRecursionDepth = maxRecursionDepth
    ).process(this)
}

/**
 *  Handles the jackson annotations "JsonIgnore", "JsonIgnoreType", "JsonIgnoreProperties", "JsonProperty".
 * See [JacksonIgnoreStep], [JacksonIgnoreTypeStep], [JacksonIgnorePropertiesStep], [JacksonPropertyStep] for more info.
 */
fun Bundle<TypeData>.handleJacksonAnnotations(): Bundle<TypeData> {
    return this
        .let { JacksonIgnoreStep().process(this) }
        .let { JacksonIgnoreTypeStep().process(this) }
        .let { JacksonIgnorePropertiesStep().process(this) }
        .let { JacksonPropertyStep().process(this) }
}



/**
 * See [JacksonJsonTypeInfoDiscriminatorStep]
 */
fun Bundle<TypeData>.addJacksonTypeInfoDiscriminatorProperty(): Bundle<TypeData> {
    return JacksonJsonTypeInfoDiscriminatorStep().process(this)
}
