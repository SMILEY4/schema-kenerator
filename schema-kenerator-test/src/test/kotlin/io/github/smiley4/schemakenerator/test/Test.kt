@file:UseSerializers(InstantSerializer::class)

package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.jsonschema.compileReferencing
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.kotest.core.spec.style.StringSpec
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import kotlin.reflect.typeOf

/**
 * internal / manual tests only
 */
class Test : StringSpec({

    "test" {

        val result = typeOf<TestRequest>()
            .processKotlinxSerialization {
                markNotParameterized<TestRequest.Item>()
            }
            .generateJsonSchema()
            .compileReferencing()

        println(result.json.prettyPrint())
        result.definitions.forEach {
            println("${it.key}:   ${it.value.prettyPrint()}")
        }

    }

}) {
    companion object {


        @Serializable
        data class TestRequest(
            val items: List<Item>? = null,
            val selectedItem: Item? = null,
        ) {
            @Serializable
            data class Item(
                val id: Int? = null,
                val name: String? = null
            )
        }

        private val json = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()!!

    }
}

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("java.time.Instant", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}