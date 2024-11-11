@file:OptIn(ExperimentalSerializationApi::class)
@file:Suppress("ClassName")

package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.annotations.Format
import io.github.smiley4.schemakenerator.core.annotations.Type
import io.github.smiley4.schemakenerator.core.connectSubTypes
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.compileReferencingRoot
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleCoreAnnotations
import io.github.smiley4.schemakenerator.swagger.handleSchemaAnnotations
import io.github.smiley4.schemakenerator.swagger.handleSwaggerAnnotations
import io.github.smiley4.schemakenerator.swagger.withTitle
import io.kotest.core.spec.style.StringSpec
import io.swagger.v3.core.util.Json31
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import java.time.Instant
import kotlin.reflect.typeOf

/**
 * internal / manual tests only
 */
class _ManualTests : StringSpec({

    "test" {
        val result = typeOf<RegisterMetadataResponse>()
            .processKotlinxSerialization {}
            .connectSubTypes()
            .generateSwaggerSchema()
            .withTitle(TitleType.SIMPLE)
            .handleSwaggerAnnotations()
            .handleCoreAnnotations()
            .handleSchemaAnnotations()
            .compileReferencingRoot()
            .asPrintable()

        println(Json31.prettyPrint(result))

    }

}) {
    companion object {

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


// Instant Serializer
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

}


// Class to be serialized and shown in the schemas
@Serializable
data class RegisterMetadataResponse(
    @Type("string")
    @Format("date-time")
    @Serializable(with = InstantSerializer::class)
    val registreringstidspunkt: Instant,
    val registrertAv: String,
)

private inline fun <reified T> createDefaultPrimitiveTypeData(type: String, format: String): PrimitiveTypeData {
    return PrimitiveTypeData(
        id = TypeId.build(T::class.qualifiedName!!),
        simpleName = T::class.simpleName!!,
        qualifiedName = T::class.qualifiedName!!,
        annotations = mutableListOf(
            AnnotationData(
                name = "type_format_annotation",
                values = mutableMapOf(
                    "type" to type,
                    "format" to format,
                ),
                annotation = null,
            ),
        ),
    )
}