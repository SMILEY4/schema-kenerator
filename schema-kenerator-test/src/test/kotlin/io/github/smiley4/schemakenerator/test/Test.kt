@file:UseSerializers(InstantSerializer::class, MyUUIDSerializer::class)

package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.withAutoTitle
import io.kotest.core.spec.style.StringSpec
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.time.Instant
import java.util.UUID
import kotlin.reflect.typeOf

/**
 * internal / manual tests only
 */
class Test : StringSpec({

    "test" {

        val JSON = Json {
            serializersModule = SerializersModule {
                contextual(UUID::class, MyUUIDSerializer)
            }
        }

        val result = typeOf<MyData>()
            .processKotlinxSerialization {
                serializersModule = JSON.serializersModule
            }
            .generateSwaggerSchema()
            .withAutoTitle(TitleType.SIMPLE)
            .compileInlining()

        println(json.writeValueAsString(result.swagger))

    }

}) {
    companion object {

        @Serializable
        class MyData(
            @Contextual
            val myId: UUID
        )


        @Serializable
        sealed class Parent(
            val fieldParent: String
        )


        @Serializable
        open class Child(
            val fieldChild: Int
        ) : Parent(fieldParent = "")


        @Serializable
        enum class Permission(val id: String) {
            @SerialName("user.write")
            USER_WRITE("user.write");
        }


        @Serializable
        data class Response(
            val something: Int,
            val permission: Permission
        )

        private val json = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()!!

    }
}

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("java.time.Instant", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}

object MyUUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
}