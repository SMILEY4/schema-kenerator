package io.github.smiley4.schemakenerator.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlin.reflect.KType
import kotlin.reflect.typeOf


inline fun <reified T> getKType(): KType {
    return typeOf<T>()
}


@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.qualifiedName() = this.serialName.replace("?", "")


@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.simpleName() = this.serialName.split(".").last().replace("?", "")
