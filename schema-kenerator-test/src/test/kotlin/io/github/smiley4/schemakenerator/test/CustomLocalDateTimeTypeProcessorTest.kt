package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonTypeHint
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAnnotationTypeHintStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileInlineStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaGenerationStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaTitleStep
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionTypeProcessingStep
import io.github.smiley4.schemakenerator.serialization.steps.KotlinxSerializationTypeProcessingStep
import io.github.smiley4.schemakenerator.swagger.data.SwaggerTypeHint
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationTypeHintStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileInlineStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaGenerationStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaTitleStep
import io.github.smiley4.schemakenerator.swagger.steps.TitleBuilder
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithLocalDateTime
import io.kotest.core.spec.style.StringSpec
import java.time.LocalDateTime
import kotlin.reflect.typeOf

class CustomLocalDateTimeTypeProcessorTest : StringSpec({

    "reflection & jsonschema: localdatetime without custom processor" {

        val result = typeOf<ClassWithLocalDateTime>()
            .let { ReflectionTypeProcessingStep().process(it) }
            .let { JsonSchemaGenerationStep().generate(it) }
            .let { JsonSchemaAnnotationTypeHintStep().process(it) }
            .let { JsonSchemaTitleStep(io.github.smiley4.schemakenerator.jsonschema.steps.TitleBuilder.BUILDER_FULL).process(it) }
            .let { JsonSchemaCompileInlineStep().compile(it) }

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
            .let {
                ReflectionTypeProcessingStep(
                    customProcessors = mapOf(LocalDateTime::class to {
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
                    })
                ).process(it)
            }
            .let { JsonSchemaGenerationStep().generate(it) }
            .let { JsonSchemaAnnotationTypeHintStep().process(it) }
            .let { JsonSchemaTitleStep(io.github.smiley4.schemakenerator.jsonschema.steps.TitleBuilder.BUILDER_FULL).process(it) }
            .let { JsonSchemaCompileInlineStep().compile(it) }

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
            .let { KotlinxSerializationTypeProcessingStep().process(it) }
            .let { SwaggerSchemaGenerationStep().generate(it) }
            .let { SwaggerSchemaAnnotationTypeHintStep().process(it) }
            .let { SwaggerSchemaTitleStep(TitleBuilder.BUILDER_FULL).process(it) }
            .let { SwaggerSchemaCompileInlineStep().compile(it) }

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
            .let {
                KotlinxSerializationTypeProcessingStep(
                    customProcessors = mapOf(LocalDateTime::class.qualifiedName!! to {
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
                    })
                ).process(it)
            }
            .let { SwaggerSchemaGenerationStep().generate(it) }
            .let { SwaggerSchemaAnnotationTypeHintStep().process(it) }
            .let { SwaggerSchemaTitleStep(TitleBuilder.BUILDER_FULL).process(it) }
            .let { SwaggerSchemaCompileInlineStep().compile(it) }

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