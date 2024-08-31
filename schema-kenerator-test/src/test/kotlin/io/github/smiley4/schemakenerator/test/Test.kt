@file:UseSerializers(InstantSerializer::class)

package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.annotations.Default
import io.github.smiley4.schemakenerator.core.annotations.Description
import io.github.smiley4.schemakenerator.core.annotations.Example
import io.github.smiley4.schemakenerator.core.annotations.Name
import io.github.smiley4.schemakenerator.core.handleNameAnnotation
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleCoreAnnotations
import io.github.smiley4.schemakenerator.swagger.withAutoTitle
import io.kotest.core.spec.style.StringSpec
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.SerialName
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

        val result = typeOf<Response>()
            .processKotlinxSerialization()
            .generateSwaggerSchema()
            .handleCoreAnnotations()
            .compileInlining()

        println(json.writeValueAsString(result.swagger))

    }

    "test issue #9" {
        val swagger = typeOf<MyExampleClass>()
            .processReflection()
            .generateSwaggerSchema()
            .handleCoreAnnotations()
            .compileInlining()
            .swagger

        println(swagger)
    }

}) {
    companion object {

        class MyExampleClass(
            @Description("Some test value.")
            @Example("Hello World")
            @Default("Default value")
            val value: String,
        )

        @Serializable
        enum class Permission(val id: String) {
            @SerialName("user.write") USER_WRITE("user.write");
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