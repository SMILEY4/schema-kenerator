package io.github.smiley4.schemakenerator.jackson.steps

import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.steps.AbstractAddDiscriminatorStep

/**
 * Handles the [JsonTypeInfo]-annotations and adds a discriminator property with the defined name and
 * annotated with a marker annotation called [io.github.smiley4.schemakenerator.core.steps.AbstractAddDiscriminatorStep.MARKER_ANNOTATION_NAME]
 */
class JacksonJsonTypeInfoDiscriminatorStep : AbstractAddDiscriminatorStep() {

    override fun getDiscriminatorPropertyName(typeData: ObjectTypeData): String? {
        val annotation = typeData.annotations.find { it.name == JsonTypeInfo::class.qualifiedName }
        if(annotation == null) {
            return null
        }
        if(!setOf("PROPERTY", "EXISTING_PROPERTY").contains(annotation.values["include"].toString())) {
            return null
        }
        return annotation.values["property"]?.toString() ?: "type"
    }

}
