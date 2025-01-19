package io.github.smiley4.schemakenerator.test

import old.AnnotationData
import old.PrimitiveTypeData
import old.TypeId
import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.data.JsonTypeHint
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.handleJsonSchemaAnnotations
import io.github.smiley4.schemakenerator.jsonschema.withTitle
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.data.SwaggerTypeHint
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleSwaggerAnnotations
import io.github.smiley4.schemakenerator.swagger.steps.TitleBuilder
import io.github.smiley4.schemakenerator.swagger.withTitle
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithLocalDateTime
import io.kotest.core.spec.style.StringSpec
import java.time.LocalDateTime
import kotlin.reflect.typeOf

class CustomLocalDateTimeTypeProcessorTest : StringSpec({

    "reflection & jsonschema: localdatetime without custom processor" {

        val result = typeOf<ClassWithLocalDateTime>()
            .processReflection()
            .generateJsonSchema()
            .handleJsonSchemaAnnotations()
            .withTitle(io.github.smiley4.schemakenerator.jsonschema.steps.TitleBuilder.BUILDER_FULL)
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
            .processReflection {
                customProcessor<LocalDateTime> {
                    PrimitiveTypeData(
                        id = TypeId.build(LocalDateTime::class.qualifiedName!!),
                        simpleName = LocalDateTime::class.simpleName!!,
                        qualifiedName = LocalDateTime::class.qualifiedName!!,
                        annotations = mutableListOf(
                            AnnotationData(
                                name = JsonTypeHint::class.qualifiedName!!,
                                values = mutableMapOf(
                                    "type" to "date"
                                ),
                                annotation = null
                            )
                        )
                    )
                }
            }
            .generateJsonSchema()
            .handleJsonSchemaAnnotations()
            .withTitle(io.github.smiley4.schemakenerator.jsonschema.steps.TitleBuilder.BUILDER_FULL)
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
                            "type": "date",
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
            .processKotlinxSerialization()
            .generateSwaggerSchema()
            .handleSwaggerAnnotations()
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
            .processKotlinxSerialization {
                customProcessor<LocalDateTime> {
                    PrimitiveTypeData(
                        id = TypeId.build(LocalDateTime::class.qualifiedName!!),
                        simpleName = LocalDateTime::class.simpleName!!,
                        qualifiedName = LocalDateTime::class.qualifiedName!!,
                        annotations = mutableListOf(
                            AnnotationData(
                                name = SwaggerTypeHint::class.qualifiedName!!,
                                values = mutableMapOf(
                                    "type" to "date"
                                ),
                                annotation = null
                            )
                        )
                    )
                }
            }
            .generateSwaggerSchema()
            .handleSwaggerAnnotations()
            .withTitle(TitleBuilder.BUILDER_FULL)
            .compileInlining()

        result.swagger.shouldEqualJson {
            """
                {
                  "title": "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithLocalDateTime",
                  "type": "object",
                  "properties": {
                    "dateTime": {
                      "type": "date",
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