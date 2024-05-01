package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.data.JsonTypeHint
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.withAutoTitle
import io.github.smiley4.schemakenerator.jsonschema.handleJsonTypeHintAnnotation
import io.github.smiley4.schemakenerator.reflection.getKType
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.data.SwaggerTypeHint
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.withAutoTitle
import io.github.smiley4.schemakenerator.swagger.handleSwaggerTypeHintAnnotation
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithLocalDateTime
import io.kotest.assertions.json.ArrayOrder
import io.kotest.assertions.json.FieldComparison
import io.kotest.assertions.json.NumberFormat
import io.kotest.assertions.json.PropertyOrder
import io.kotest.assertions.json.TypeCoercion
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.StringSpec
import java.time.LocalDateTime

class CustomLocalDateTimeTypeProcessorTest : StringSpec({

    "reflection & jsonschema: localdatetime without custom processor" {

        val result = listOf(getKType<ClassWithLocalDateTime>())
            .processReflection()
            .generateJsonSchema()
            .withAutoTitle()
            .compileInlining()
            .first()

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

        val result = listOf(getKType<ClassWithLocalDateTime>())
            .processReflection {
                customProcessor(LocalDateTime::class) {
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
            .withAutoTitle()
            .handleJsonTypeHintAnnotation()
            .compileInlining()
            .first()

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

        val result = listOf(getKType<io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithLocalDateTime>())
            .processKotlinxSerialization()
            .generateSwaggerSchema()
            .withAutoTitle()
            .compileInlining()
            .first()

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

        val result = listOf(getKType<io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithLocalDateTime>())
            .processKotlinxSerialization {
                customProcessor(LocalDateTime::class) {
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
            .withAutoTitle()
            .handleSwaggerTypeHintAnnotation()
            .compileInlining()
            .first()

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