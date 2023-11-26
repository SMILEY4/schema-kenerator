package io.github.smiley4.schemakenerator.data

import io.github.smiley4.schemakenerator.getKType
import kotlin.reflect.KType

data class PropertyInformation(
    val type: KType,
    val name: String,
    val typeInformation: TypeInformation,
) {

    companion object {
        val UNKNOWN = PropertyInformation(
            type = getKType<PropertyInformation>(),
            name = "?",
            typeInformation = TypeInformation.UNKNOWN
        )
    }

}