package io.github.smiley4.schemakenerator.jackson

import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.smiley4.schemakenerator.core.DiscriminatorNameProvider
import io.github.smiley4.schemakenerator.core.data.TypeData

internal class JacksonJsonTypeInfoDiscriminatorNameProvider : DiscriminatorNameProvider {

    override fun getDiscriminatorPropertyName(typeData: TypeData): String? {
        val annotation = typeData.annotations.find { it.name == JsonTypeInfo::class.qualifiedName }
        if (annotation == null) {
            return null
        }
        if (!setOf("PROPERTY", "EXISTING_PROPERTY").contains(annotation.values["include"].toString())) {
            return null
        }
        return annotation.values["property"]?.toString() ?: "type"
    }

}
