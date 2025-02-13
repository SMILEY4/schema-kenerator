@file:OptIn(ExperimentalSerializationApi::class)
@file:Suppress("ClassName")


package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.addDiscriminatorProperty
import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.withTitle
import io.github.smiley4.schemakenerator.reflection.analyseTypeUsingReflection
import io.github.smiley4.schemakenerator.serialization.addJsonClassDiscriminatorProperty
import io.github.smiley4.schemakenerator.serialization.analyzeTypeUsingKotlinxSerialization
import io.github.smiley4.schemakenerator.serialization.renameMembers
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.kotest.core.spec.style.StringSpec
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.util.UUID
import kotlin.reflect.typeOf

/**
 * internal / manual tests only
 */
class _ManualTests : StringSpec({

    "test" {
        val jsonSchema = typeOf<MyExampleClass>()
            .analyseTypeUsingReflection()
            .generateJsonSchema()
            .withTitle(TitleType.SIMPLE)
            .compileInlining()
            .json
            .prettyPrint()
        println(jsonSchema)
    }

}) {
    companion object {

        class MyExampleClass(
            val someText: String,
            val someNullableInt: Int?,
            val someBoolList: List<Boolean>,
        )

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