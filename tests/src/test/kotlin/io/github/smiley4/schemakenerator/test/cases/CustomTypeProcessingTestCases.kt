package io.github.smiley4.schemakenerator.test.cases

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.TypeName
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.typeOf

object CustomTypeProcessingTestCases {

    val localDateTimeWithoutConfig = case("custom type processing", "LocalDateTime without additional config") {
        type = typeOf<ClassWithLocalDateTime>()
        // language=json
        expectedSwaggerInline = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "dateTime" : {
                      "type" : "object",
                      "properties" : { }
                    }
                  },
                  "required" : [ "dateTime" ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedSwaggerReference = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "dateTime" : {
                      "${'$'}ref" : "#/components/schemas/LocalDateTime"
                    }
                  },
                  "required" : [ "dateTime" ]
                },
                "LocalDateTime" : {
                  "type" : "object",
                  "properties" : { }
                }
              }
            }
            """.trimIndent()

        // language=json
        expectedJsonInline = """
            {
               "type": "object",
               "required": [
                  "dateTime"
               ],
               "properties": {
                  "dateTime": {
                     "type": "object",
                     "required": [],
                     "properties": {}
                  }
               }
            }
            """.trimIndent()
        // language=json
        expectedJsonReference = """
            {
               "type": "object",
               "required": [
                  "dateTime"
               ],
               "properties": {
                  "dateTime": {
                     "${'$'}ref": "#/${'$'}defs/LocalDateTime"
                  }
               },
               "${'$'}defs": {
                  "LocalDateTime": {
                     "type": "object",
                     "required": [],
                     "properties": {}
                  }
               }
            }
            """.trimIndent()
    }

    val localDateTimeWithCustomProcessor = case("custom type processing", "LocalDateTime with custom type") {
        type = typeOf<ClassWithLocalDateTime>()
        reflectionConfig = {
            custom<LocalDateTime> {
                TypeData(
                    id = TypeId.create(),
                    identifyingName = TypeName("kotlin.String", "String"),
                    descriptiveName = TypeName(LocalDateTime::class.qualifiedName!!, LocalDateTime::class.simpleName!!),
                    typeParameters = mutableListOf(),
                    annotations = mutableListOf(),
                    subtypes = mutableListOf(),
                    supertypes = mutableListOf(),
                    members = mutableListOf(),
                    isInlineValue = false,
                    enumData = null,
                    collectionData = null,
                    mapData = null
                )
            }
        }
        kotlinxSerializationConfig = {
            custom<LocalDateTime> {
                TypeData(
                    id = TypeId.create(),
                    identifyingName = TypeName("kotlin.String", "String"),
                    descriptiveName = TypeName(LocalDateTime::class.qualifiedName!!, LocalDateTime::class.simpleName!!),
                    typeParameters = mutableListOf(),
                    annotations = mutableListOf(),
                    subtypes = mutableListOf(),
                    supertypes = mutableListOf(),
                    members = mutableListOf(),
                    isInlineValue = false,
                    enumData = null,
                    collectionData = null,
                    mapData = null
                )
            }
        }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "dateTime" : {
                      "type" : "string"
                    }
                  },
                  "required" : [ "dateTime" ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
               "type": "object",
               "required": [
                  "dateTime"
               ],
               "properties": {
                  "dateTime": {
                     "type": "string"
                  }
               }
            }
            """.trimIndent()
    }


    @Serializable
    private class ClassWithLocalDateTime(
        @Serializable(with = CustomLocalDateTimeSerializer::class)
        val dateTime: LocalDateTime
    )


    @OptIn(ExperimentalSerializationApi::class)
    @Serializer(forClass = LocalDateTime::class)
    private object CustomLocalDateTimeSerializer : KSerializer<LocalDateTime> {
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        override fun serialize(encoder: Encoder, value: LocalDateTime) {
            encoder.encodeString(value.format(formatter))
        }

        override fun deserialize(decoder: Decoder): LocalDateTime {
            return LocalDateTime.parse(decoder.decodeString(), formatter)
        }
    }

}