@file:UseSerializers(InstantSerializer::class, MyUUIDSerializer::class)
@file:OptIn(ExperimentalSerializationApi::class)
@file:Suppress("ClassName")


package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.withTitle
import io.github.smiley4.schemakenerator.serialization.analyzeTypeUsingKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.kotest.core.spec.style.StringSpec
import kotlinx.serialization.Contextual
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
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.modules.SerializersModule
import java.time.Instant
import java.util.UUID
import kotlin.reflect.typeOf

/**
 * internal / manual tests only
 */
class _ManualTests : StringSpec({

    "test" {

        val kotlinxJson = Json {
            // todo: support all these values (that make sense)
            encodeDefaults = true
            ignoreUnknownKeys = true
            isLenient = true
            allowStructuredMapKeys = true
            prettyPrint = true
            explicitNulls = true
            prettyPrintIndent = ""
            coerceInputValues = true
            useArrayPolymorphism = true
            classDiscriminator = "type"
            classDiscriminatorMode = ClassDiscriminatorMode.ALL_JSON_OBJECTS
            allowSpecialFloatingPointValues = true
            useAlternativeNames = true
            namingStrategy = JsonNamingStrategy.KebabCase
            decodeEnumsCaseInsensitive = true
            allowTrailingComma = true
            allowComments = true
            serializersModule = SerializersModule {
                contextual(UUID::class, MyUUIDSerializer)
            }
        }

        kotlinxJson.configuration

        println(kotlinxJson.encodeToString(MyData(UUID.randomUUID())))
        println()

        val result = typeOf<MyData>()
            .analyzeTypeUsingKotlinxSerialization {
                json = kotlinxJson
            }
            .also {
                println(it)
            }
            .generateJsonSchema()
            .withTitle(TitleType.SIMPLE)
            .compileInlining()
            .json
            .prettyPrint()

        println(result)
    }

}) {
    companion object {

        @Serializable
        class MyData(
            @Contextual
            val myId: UUID
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