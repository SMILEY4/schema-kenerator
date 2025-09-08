package io.github.smiley4.schemakenerator.serialization

import io.github.smiley4.schemakenerator.core.DiscriminatorNameProvider
import io.github.smiley4.schemakenerator.core.data.TypeData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonClassDiscriminator

internal class KotlinxJsonDiscriminatorNameProvider : DiscriminatorNameProvider {

    @OptIn(ExperimentalSerializationApi::class)
    override fun getDiscriminatorPropertyName(typeData: TypeData): String? {
        val annotation = typeData.annotations.find { it.name == JsonClassDiscriminator::class.qualifiedName }
        if(annotation == null) {
            return null
        }
        return annotation.values["discriminator"]?.toString() ?: "type"
    }

}
