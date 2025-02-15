package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.TypeName
import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.withTitle
import io.github.smiley4.schemakenerator.reflection.analyzeTypeUsingReflection
import io.github.smiley4.schemakenerator.serialization.analyzeTypeUsingKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.TitleBuilder
import io.github.smiley4.schemakenerator.swagger.withTitle
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithLocalDateTime
import io.kotest.core.spec.style.StringSpec
import java.time.LocalDateTime
import kotlin.reflect.typeOf

class CustomLocalDateTimeTypeProcessorTest : StringSpec({

    "reflection & jsonschema: localdatetime without custom processor" {

        val result = typeOf<ClassWithLocalDateTime>()
            .analyzeTypeUsingReflection()
            .generateJsonSchema()
            .withTitle(io.github.smiley4.schemakenerator.jsonschema.TitleBuilder.BUILDER_FULL)
            .compileInlining()

        result.json.shouldEqualJson {
            """
                {
                    "type": "object",
                    "title": "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithLocalDateTime",
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

        val result = typeOf<ClassWithLocalDateTime>()
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
                    "title": "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithLocalDateTime"
                }
            """.trimIndent()
        }

    }



    "kotlinx-serialization & swagger: localdatetime without custom processor" {

        val result = typeOf<io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithLocalDateTime>()
            .analyzeTypeUsingKotlinxSerialization()
            .generateSwaggerSchema()
            .withTitle(TitleBuilder.BUILDER_FULL)
            .compileInlining()

        result.swagger.shouldEqualJson {
            """
                {
                  "title": "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithLocalDateTime",
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

        val result = typeOf<io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithLocalDateTime>()
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

        result.swagger.shouldEqualJson {
            """
                {
                  "title": "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithLocalDateTime",
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
})