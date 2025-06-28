package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.core.CoreSteps.initial
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.TypeName
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.withTitle
import io.github.smiley4.schemakenerator.reflection.ReflectionSteps.analyzeTypeUsingReflection
import io.github.smiley4.schemakenerator.serialization.SerializationSteps.analyzeTypeUsingKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.compileInlining
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.withTitle
import io.github.smiley4.schemakenerator.swagger.TitleBuilder
import io.kotest.core.spec.style.StringSpec
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CustomLocalDateTimeTypeProcessorTest : StringSpec({

    "reflection & jsonschema: localdatetime without custom processor" {

        val result = initial<ClassWithLocalDateTime>()
            .analyzeTypeUsingReflection()
            .generateJsonSchema()
            .withTitle(io.github.smiley4.schemakenerator.jsonschema.TitleBuilder.BUILDER_FULL)
            .compileInlining()

        result.json.shouldEqualJson {
            """
                {
                    "type": "object",
                    "title": "io.github.smiley4.schemakenerator.test.CustomLocalDateTimeTypeProcessorTest.Companion.ClassWithLocalDateTime",
                    "required": [
                        "dateTime"
                    ],
                    "properties": {
                        "dateTime": {
                            "title": "java.time.LocalDateTime",
                            "type": "object",
                            "required": [],
                            "properties": {}
                        }
                    }
                }
            """.trimIndent()
        }
    }

    "reflection & jsonschema: localdatetime with custom processor" {

        val result = initial<ClassWithLocalDateTime>()
            .analyzeTypeUsingReflection {
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
            .generateJsonSchema()
            .withTitle(io.github.smiley4.schemakenerator.jsonschema.TitleBuilder.BUILDER_FULL)
            .compileInlining()

        // language=json
        result.json.shouldEqualJson {
            """
                {
                    "type": "object",
                    "required": [
                        "dateTime"
                    ],
                    "properties": {
                        "dateTime": {
                            "type": "string",
                            "title": "java.time.LocalDateTime"
                        }
                    },
                    "title": "io.github.smiley4.schemakenerator.test.CustomLocalDateTimeTypeProcessorTest.Companion.ClassWithLocalDateTime"
                }
            """.trimIndent()
        }

    }



    "kotlinx-serialization & swagger: localdatetime without custom processor" {

        val result = initial<ClassWithLocalDateTime>()
            .analyzeTypeUsingKotlinxSerialization()
            .generateSwaggerSchema()
            .withTitle(TitleBuilder.BUILDER_FULL)
            .compileInlining()

        // language=json
        result.swagger.shouldEqualJson {
            """
                {
                  "title": "io.github.smiley4.schemakenerator.test.CustomLocalDateTimeTypeProcessorTest.Companion.ClassWithLocalDateTime",
                  "type": "object",
                  "properties": {
                    "dateTime": {
                      "type": "object",
                      "properties": {},
                      "title": "java.time.LocalDateTime"
                    }
                  },
                  "required": [
                    "dateTime"
                  ]
                }
            """.trimIndent()
        }
    }

    "kotlinx-serialization & swagger: localdatetime with custom processor" {

        val result = initial<ClassWithLocalDateTime>()
            .analyzeTypeUsingKotlinxSerialization {
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
            .generateSwaggerSchema()
            .withTitle(TitleBuilder.BUILDER_FULL)
            .compileInlining()

        // language=json
        result.swagger.shouldEqualJson {
            """
                {
                  "title": "io.github.smiley4.schemakenerator.test.CustomLocalDateTimeTypeProcessorTest.Companion.ClassWithLocalDateTime",
                  "type": "object",
                  "properties": {
                    "dateTime": {
                      "type": "string",
                      "title": "java.time.LocalDateTime"
                    }
                  },
                  "required": [
                    "dateTime"
                  ]
                }
            """.trimIndent()
        }

    }
}) {
    companion object {

        @Serializable
        class ClassWithLocalDateTime(
            @Serializable(with = LocalDateTimeSerializer::class)
            val dateTime: LocalDateTime
        )


        @OptIn(ExperimentalSerializationApi::class)
        @Serializer(forClass = LocalDateTime::class)
        object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
            private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

            override fun serialize(encoder: Encoder, value: LocalDateTime) {
                encoder.encodeString(value.format(formatter))
            }

            override fun deserialize(decoder: Decoder): LocalDateTime {
                return LocalDateTime.parse(decoder.decodeString(), formatter)
            }
        }

    }
}