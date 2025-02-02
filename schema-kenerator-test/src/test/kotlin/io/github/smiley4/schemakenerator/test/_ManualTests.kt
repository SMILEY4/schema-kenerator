@file:OptIn(ExperimentalSerializationApi::class)
@file:Suppress("ClassName")


package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.addDiscriminatorProperty
import io.github.smiley4.schemakenerator.serialization.addJsonClassDiscriminatorProperty
import io.github.smiley4.schemakenerator.serialization.analyzeTypeUsingKotlinxSerialization
import io.github.smiley4.schemakenerator.serialization.renameMembers
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.kotest.core.spec.style.StringSpec
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID
import kotlin.reflect.typeOf

/**
 * internal / manual tests only
 */
class _ManualTests : StringSpec({

    "test" {

        val kotlinxJson = Json {
            // ignore
            prettyPrint = true
//            useArrayPolymorphism = true

            // type analysis
//            classDiscriminator = "type"
//            classDiscriminatorMode = ClassDiscriminatorMode.ALL_JSON_OBJECTS
//            useAlternativeNames = true
//            namingStrategy = JsonNamingStrategy.KebabCase

            // schema generation
//            encodeDefaults = true
            allowStructuredMapKeys = true // see https://petnagy.medium.com/kotlinx-serialization-part2-d6c23f7839c4
//            explicitNulls = true
//            allowSpecialFloatingPointValues = true

//            serializersModule = SerializersModule {
//                contextual(UUID::class, MyUUIDSerializer)
//            }
        }

        println(kotlinxJson.encodeToString(MyData(
            attributes = mapOf(
                CombinedKey(
                    a = "1",
                    b = 1
                ) to 11,
                CombinedKey(
                    a = "2",
                    b = 2
                ) to 12
            )
        )))

        println(kotlinxJson.encodeToString(MyData2(
            attributes = mapOf(
                1 to 11,
                2 to 12
            )
        )))



        /*
        todo: open questions
        - new steps or add config to existing steps ?
        - how to reduce amount of different steps ?
        - overall "pipeline" config that all steps can draw from ?
         */

        val result = typeOf<MyData>()
            .analyzeTypeUsingKotlinxSerialization {
                // use
                // * kotlinxJson.serializersModule
                json = kotlinxJson
            }
            // use
            // * kotlinxJson.configuration.classDiscriminator
            // * kotlinxJson.configuration.classDiscriminatorMode
            .addDiscriminatorProperty(
                discriminatorPropertyName = kotlinxJson.configuration.classDiscriminator
            )
            .addJsonClassDiscriminatorProperty()
            // use
            // * kotlinxJson.configuration.useAlternativeNames
            // * kotlinxJson.configuration.namingStrategy
            .renameMembers(TODO())
            // use
            // * kotlinxJson.configuration.encodeDefaults
            // * kotlinxJson.configuration.allowStructuredMapKeys
            // * kotlinxJson.configuration.explicitNulls
            // * kotlinxJson.configuration.allowSpecialFloatingPointValues
            .generateSwaggerSchema()
            .compileInlining()
            .asPrintable()

        println(json.writeValueAsString(result))
    }

}) {
    companion object {

        @Serializable
        class MyData(
            val attributes: Map<CombinedKey, Int>
        )

        @Serializable
        class MyData2(
            val attributes: Map<Int, Int>
        )

        @Serializable
        data class CombinedKey(
            val a: String,
            val b: Int,
        )

        class SwaggerResult(
            val root: io.swagger.v3.oas.models.media.Schema<*>,
            val componentSchemas: Map<String, io.swagger.v3.oas.models.media.Schema<*>>
        )

        fun CompiledSwaggerSchema.asPrintable(): SwaggerResult {
            return SwaggerResult(
                root = this.swagger,
                componentSchemas = this.componentSchemas
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

object MyUUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
}