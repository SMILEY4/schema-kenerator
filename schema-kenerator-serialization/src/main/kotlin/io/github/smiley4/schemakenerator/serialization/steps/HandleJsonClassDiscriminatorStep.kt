package io.github.smiley4.schemakenerator.serialization.steps

import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.steps.AbstractAddDiscriminatorStep
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * Handles the [JsonClassDiscriminator]-annotations and adds a discriminator property with the defined name and
 * annotated with a marker annotation called [io.github.smiley4.schemakenerator.core.steps.AbstractAddDiscriminatorStep.MARKER_ANNOTATION_NAME]
 */
class HandleJsonClassDiscriminatorStep : AbstractAddDiscriminatorStep() {

    @OptIn(ExperimentalSerializationApi::class)
    override fun getDiscriminatorPropertyName(typeData: ObjectTypeData): String? {
        val annotation = typeData.annotations.find { it.name == JsonClassDiscriminator::class.qualifiedName }
        if(annotation == null) {
            return null
        }
        return annotation.values["discriminator"]?.toString() ?: "type"
    }

}
