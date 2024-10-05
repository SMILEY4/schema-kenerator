package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleSchemaAnnotations
import io.github.smiley4.schemakenerator.swagger.handleSwaggerAnnotations
import io.github.smiley4.schemakenerator.validation.swagger.handleJakartaValidationAnnotations
import io.github.smiley4.schemakenerator.validation.swagger.handleJavaxValidationAnnotations
import io.kotest.assertions.json.ArrayOrder
import io.kotest.assertions.json.FieldComparison
import io.kotest.assertions.json.NumberFormat
import io.kotest.assertions.json.PropertyOrder
import io.kotest.assertions.json.TypeCoercion
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.StringSpec
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import kotlin.reflect.typeOf

class SwaggerAnnotationsTest : StringSpec({

    "swagger annotations" {
        val result = typeOf<MyTestClass>()
            .processReflection()
            .generateSwaggerSchema()
            .handleSwaggerAnnotations()
            .handleSchemaAnnotations()
            .compileInlining()
        jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()
            .writeValueAsString(result.swagger).shouldEqualJson {
                propertyOrder = PropertyOrder.Lenient
                arrayOrder = ArrayOrder.Lenient
                fieldComparison = FieldComparison.Strict
                numberFormat = NumberFormat.Lenient
                typeCoercion = TypeCoercion.Disabled
                """
                {
                  "title": "My Test Class",
                  "required": [
                    "myValue",
                    "someTags"
                  ],
                  "type": "object",
                  "properties": {
                    "myValue": {
                      "title": "Some Value",
                      "maximum": 9,
                      "exclusiveMaximum": true,
                      "minimum": 0,
                      "exclusiveMinimum": false,
                      "maxLength": 10,
                      "minLength": 1,
                      "type": "integer",
                      "description": "some value",
                      "format": "single-digit",
                      "readOnly": true,
                      "exampleSetFlag": false,
                      "enum": ["1","2","3","4"],
                      "default": "1"
                    },
                    "someTags": {
                      "maxItems": 10,
                      "minItems": 0,
                      "uniqueItems": true,
                      "type": "array",
                      "exampleSetFlag": false,
                      "items": {
                        "type": "string",
                        "exampleSetFlag": false
                      }
                    }
                  },
                  "description": "some description",
                  "exampleSetFlag": false
                }
            """.trimIndent()
            }
    }

    "partially specified class" {
        shouldNotThrowAny {
            typeOf<PartiallySpecified>()
                .processReflection()
                .generateSwaggerSchema()
                .handleSchemaAnnotations()
                .compileInlining()
        }
    }

    "hidden fields with no required fields" {
        shouldNotThrowAny {
            typeOf<AllOptionalFields>()
                .processReflection()
                .generateSwaggerSchema()
                .handleSchemaAnnotations()
                .compileInlining()
        }
    }

    "javax validations used to create schema" {
        val result = typeOf<Validated>()
            .processReflection()
            .generateSwaggerSchema()
            .handleJavaxValidationAnnotations()
            .compileInlining()
        jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(result.swagger)
            .shouldEqualJson {
                propertyOrder = PropertyOrder.Lenient
                arrayOrder = ArrayOrder.Lenient
                fieldComparison = FieldComparison.Strict
                numberFormat = NumberFormat.Lenient
                typeCoercion = TypeCoercion.Disabled
                """
                {
                  "required": [
                    "hasSize",
                    "minMax",
                    "mustNotBeBlank",
                    "mustNotBeEmpty",
                    "mustNotBeNull"
                  ],
                  "type": "object",
                  "properties": {
                    "hasSize": {
                      "maxLength": 95,
                      "minLength": 4,
                      "type": "string",
                      "exampleSetFlag": false
                    },
                    "minMax": {
                      "maximum": 10,
                      "minimum": 5,
                      "type": "integer",
                      "format": "int32",
                      "exampleSetFlag": false
                    },
                    "mustNotBeBlank": {
                      "type": "string",
                      "exampleSetFlag": false
                    },
                    "mustNotBeEmpty": {
                      "type": "string",
                      "exampleSetFlag": false
                    },
                    "mustNotBeNull": {
                      "type": "object",
                      "exampleSetFlag": false
                    }
                  },
                  "exampleSetFlag": false
                }
            """.trimIndent()
            }
    }

    "jakarta validations used to create schema" {
        val result = typeOf<JakartaValidated>()
            .processReflection()
            .generateSwaggerSchema()
            .handleJakartaValidationAnnotations()
            .compileInlining()
        jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(result.swagger)
            .shouldEqualJson {
                propertyOrder = PropertyOrder.Lenient
                arrayOrder = ArrayOrder.Lenient
                fieldComparison = FieldComparison.Strict
                numberFormat = NumberFormat.Lenient
                typeCoercion = TypeCoercion.Disabled
                """
                {
                  "required": [
                    "hasSize",
                    "minMax",
                    "mustNotBeBlank",
                    "mustNotBeEmpty",
                    "mustNotBeNull"
                  ],
                  "type": "object",
                  "properties": {
                    "hasSize": {
                      "maxLength": 95,
                      "minLength": 4,
                      "type": "string",
                      "exampleSetFlag": false
                    },
                    "minMax": {
                      "maximum": 10,
                      "minimum": 5,
                      "type": "integer",
                      "format": "int32",
                      "exampleSetFlag": false
                    },
                    "mustNotBeBlank": {
                      "type": "string",
                      "exampleSetFlag": false
                    },
                    "mustNotBeEmpty": {
                      "type": "string",
                      "exampleSetFlag": false
                    },
                    "mustNotBeNull": {
                      "type": "object",
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

        @Schema(
            description = "some description",
            title = "My Test Class",
            name = "TestClass",
        )
        private class MyTestClass(

            @field:Schema(
                description = "some value",
                title = "Some Value",
                name = "someValue",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = ["1", "2", "3", "4"],
                defaultValue = "1",
                accessMode = Schema.AccessMode.READ_ONLY,
                minLength = 1,
                maxLength = 10,
                format = "single-digit",
                minimum = "0",
                maximum = "9",
                exclusiveMaximum = true
            )
            val myValue: Int,

            @field:Schema(
                hidden = true,
                name = "hidden-value"
            )
            val hiddenValue: String,

            @field:ArraySchema(
                minItems = 0,
                maxItems = 10,
                uniqueItems = true
            )
            val someTags: List<String>
        )

    }

    private class PartiallySpecified(
        @field:Schema(description = "Mysterious thing")
        val x: String,
    )

    private class AllOptionalFields(
        @field:Schema(description = "The first field")
        val firstField: String?,
        @field:Schema(hidden = true)
        val secondField: Int?,
        @field:Schema(hidden = true)
        val thirdField: Boolean?
    )

    private class Validated(
        @field:Min(5)
        @field:Max(10)
        val minMax: Int,
        @field:NotNull
        val mustNotBeNull: Any?,
        @field:NotEmpty
        val mustNotBeEmpty: String?,
        @field:NotBlank
        val mustNotBeBlank: String?,
        @field:Size(min = 4, max = 95)
        val hasSize: String
    )

    private class JakartaValidated(
        @field:jakarta.validation.constraints.Min(5)
        @field:jakarta.validation.constraints.Max(10)
        val minMax: Int,
        @field:jakarta.validation.constraints.NotNull
        val mustNotBeNull: Any?,
        @field:jakarta.validation.constraints.NotEmpty
        val mustNotBeEmpty: String?,
        @field:jakarta.validation.constraints.NotBlank
        val mustNotBeBlank: String?,
        @field:jakarta.validation.constraints.Size(min = 4, max = 95)
        val hasSize: String
    )

}