package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonTypeHint
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAnnotationTypeHintStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAutoTitleStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaGenerationStep
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionTypeProcessingStep
import io.github.smiley4.schemakenerator.serialization.steps.KotlinxSerializationTypeProcessingStep
import io.github.smiley4.schemakenerator.swagger.data.SwaggerTypeHint
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationTypeHintStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAutoTitleStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaGenerationStep
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithLocalDateTime
import io.kotest.assertions.json.ArrayOrder
import io.kotest.assertions.json.FieldComparison
import io.kotest.assertions.json.NumberFormat
import io.kotest.assertions.json.PropertyOrder
import io.kotest.assertions.json.TypeCoercion
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.StringSpec
import java.time.LocalDateTime
import kotlin.reflect.typeOf

class CustomLocalDateTimeTypeProcessorTest : StringSpec({

    "reflection & jsonschema: localdatetime without custom processor" {

        val result = typeOf<ClassWithLocalDateTime>()
            .let { ReflectionTypeProcessingStep().process(it) }
            .let { JsonSchemaGenerationStep().generate(it) }
            .let { JsonSchemaAnnotationTypeHintStep().process(it) }
            .let { JsonSchemaAutoTitleStep(TitleType.FULL).process(it) }
            .let { JsonSchemaCompileStep().compileInlining(it) }

        result.json.prettyPrint().shouldEqualJson {
            propertyOrder = PropertyOrder.Lenient
            arrayOrder = ArrayOrder.Lenient
            fieldComparison = FieldComparison.Strict
            numberFormat = NumberFormat.Lenient
            typeCoercion = TypeCoercion.Disabled
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
            .let { JsonSchemaAutoTitleStep(TitleType.FULL).process(it) }
            .let { JsonSchemaCompileStep().compileInlining(it) }

        result.json.prettyPrint().shouldEqualJson {
            propertyOrder = PropertyOrder.Lenient
            arrayOrder = ArrayOrder.Lenient
            fieldComparison = FieldComparison.Strict
            numberFormat = NumberFormat.Lenient
            typeCoercion = TypeCoercion.Disabled
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
            .let { SwaggerSchemaAutoTitleStep(io.github.smiley4.schemakenerator.swagger.data.TitleType.FULL).process(it) }
            .let { SwaggerSchemaCompileStep().compileInlining(it) }

        json.writeValueAsString(result.swagger).shouldEqualJson {
            propertyOrder = PropertyOrder.Lenient
            arrayOrder = ArrayOrder.Lenient
            fieldComparison = FieldComparison.Strict
            numberFormat = NumberFormat.Lenient
            typeCoercion = TypeCoercion.Disabled
            """
                {
                    "title": "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithLocalDateTime",
                    "required": [
                        "dateTime"
                    ],
                    "type": "object",
                    "properties": {
                        "dateTime": {
                            "type": "object",
                            "title": "java.time.LocalDateTime",
                            "properties": {},
                            "exampleSetFlag": false
                        }
                    },
                    "exampleSetFlag": false
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
            .let { SwaggerSchemaAutoTitleStep(io.github.smiley4.schemakenerator.swagger.data.TitleType.FULL).process(it) }
            .let { SwaggerSchemaCompileStep().compileInlining(it) }

        json.writeValueAsString(result.swagger).shouldEqualJson {
            propertyOrder = PropertyOrder.Lenient
            arrayOrder = ArrayOrder.Lenient
            fieldComparison = FieldComparison.Strict
            numberFormat = NumberFormat.Lenient
            typeCoercion = TypeCoercion.Disabled
            """
                {
                    "title": "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithLocalDateTime",
                    "required": [
                        "dateTime"
                    ],
                    "type": "object",
                    "properties": {
                        "dateTime": {
                            "type": "date",
                            "title": "java.time.LocalDateTime",
                            "exampleSetFlag": false
                        }
                    },
                    "exampleSetFlag": false
                }
            """.trimIndent()
        }

    }

}) {
    companion object {
        private val json = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()!!
    }
}