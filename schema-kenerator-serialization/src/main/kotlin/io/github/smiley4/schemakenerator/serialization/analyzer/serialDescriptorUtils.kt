package io.github.smiley4.schemakenerator.serialization.analyzer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nonNullOriginal
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * @return the name for this serial descriptor without modifiers (e.g. nullable)
 */
@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.fullName() = this.nonNullOriginal.serialName

/**
 * Get the serializer (or null) for the given type
 */
fun getSerializerFor(type: KType): KSerializer<Any?>? {
    return if (type.classifier is KClass<*>) {
        try {
            serializerOrNull(type)
        } catch (ignore: IllegalArgumentException) {
            null
        }
    } else {
        throw IllegalArgumentException("Type '$type' is not a class.")
    }
}
