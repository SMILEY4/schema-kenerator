package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.CoreSteps.initial
import io.github.smiley4.schemakenerator.core.data.InitialKTypeData
import io.github.smiley4.schemakenerator.reflection.ReflectionSteps.analyzeTypeUsingReflection
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.compileInlining
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.handleSchemaAnnotations
import io.github.smiley4.schemakenerator.validation.swagger.ValidationSwaggerSteps.handleJakartaValidationAnnotations
import io.github.smiley4.schemakenerator.validation.swagger.ValidationSwaggerSteps.handleJavaxValidationAnnotations
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
        val result = initial<MyTestClass>()
            .analyzeTypeUsingReflection()
            .generateSwaggerSchema()
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
                    "emptyAnnotation",
                    "someValue",
                    "someTags"
                  ],
                  "types": ["object"],
                  "properties": {
                    "emptyAnnotation": {
                      "exampleSetFlag": false,
                      "types": [ "string" ]
                    },
                    "someValue": {
                      "title": "Some Value",
                      "maximum": 9,
                      "exclusiveMaximum": true,
                      "minimum": 0,
                      "maxLength": 10,
                      "minLength": 1,
                      "types": ["integer"],
                      "description": "some value",
                      "example": "5",
                      "format": "single-digit",
                      "readOnly": true,
                      "exampleSetFlag": true,
                      "enum": ["1","2","3","4"],
                      "default": "1"
                    },
                    "someTags": {
                      "maxItems": 10,
                      "minItems": 0,
                      "uniqueItems": true,
                      "types": ["array"],
                      "exampleSetFlag": false,
                      "items": {
                        "types": ["string"],
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
            initial<PartiallySpecified>()
                .analyzeTypeUsingReflection()
                .generateSwaggerSchema()
                .handleSchemaAnnotations()
                .compileInlining()
        }
    }

    "hidden fields with no required fields" {
        shouldNotThrowAny {
            initial<AllOptionalFields>()
                .analyzeTypeUsingReflection()
                .generateSwaggerSchema()
                .handleSchemaAnnotations()
                .compileInlining()
        }
    }

    "javax validations used to create schema" {
        val result = initial<Validated>()
            .analyzeTypeUsingReflection()
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
                  "types": ["object"],
                  "properties": {
                    "hasSize": {
                      "maxLength": 95,
                      "minLength": 4,
                      "types": ["string"],
                      "exampleSetFlag": false
                    },
                    "minMax": {
                      "maximum": 10,
                      "minimum": 5,
                      "types": ["integer"],
                      "format": "int32",
                      "exampleSetFlag": false
                    },
                    "mustNotBeBlank": {
                      "types": ["string"],
                      "exampleSetFlag": false
                    },
                    "mustNotBeEmpty": {
                      "types": ["string"],
                      "exampleSetFlag": false
                    },
                    "mustNotBeNull": {
                      "types": ["object"],
                      "exampleSetFlag": false
                    }
                  },
                  "exampleSetFlag": false
                }
            """.trimIndent()
            }
    }

    "jakarta validations used to create schema" {
        val result = initial<JakartaValidated>()
            .analyzeTypeUsingReflection()
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
                  "types": ["object"],
                  "properties": {
                    "hasSize": {
                      "maxLength": 95,
                      "minLength": 4,
                      "types": ["string"],
                      "exampleSetFlag": false
                    },
                    "minMax": {
                      "maximum": 10,
                      "minimum": 5,
                      "types": ["integer"],
                      "format": "int32",
                      "exampleSetFlag": false
                    },
                    "mustNotBeBlank": {
                      "types": ["string"],
                      "exampleSetFlag": false
                    },
                    "mustNotBeEmpty": {
                      "types": ["string"],
                      "exampleSetFlag": false
                    },
                    "mustNotBeNull": {
                      "types": ["object"],
                      "exampleSetFlag": false
                    }
                  },
                  "exampleSetFlag": false
                }
            """.trimIndent()
            }
    }

    "javax and jakarta required annotations for all null properties" {
        var result = initial<NotNullWithAllNullProperties>()
            .analyzeTypeUsingReflection()
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
                    "requiredProperty"
                  ],
                  "types": ["object"],
                  "properties": {
                    "requiredProperty": {
                      "types": ["string"],
                      "exampleSetFlag": false
                    }
                  },
                  "exampleSetFlag": false
                }
            """.trimIndent()
            }
        result = initial<NotEmptyWithAllNullProperties>()
            .analyzeTypeUsingReflection()
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
                    "requiredProperty"
                  ],
                  "types": ["object"],
                  "properties": {
                    "requiredProperty": {
                      "types": ["string"],
                      "exampleSetFlag": false
                    }
                  },
                  "exampleSetFlag": false
                }
            """.trimIndent()
            }
        result = initial<NotBlankWithAllNullProperties>()
            .analyzeTypeUsingReflection()
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
                    "requiredProperty"
                  ],
                  "types": ["object"],
                  "properties": {
                    "requiredProperty": {
                      "types": ["string"],
                      "exampleSetFlag": false
                    }
                  },
                  "exampleSetFlag": false
                }
            """.trimIndent()
            }
        result = initial<JakartaNotNullWithAllNullProperties>()
            .analyzeTypeUsingReflection()
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
                    "requiredProperty"
                  ],
                  "types": ["object"],
                  "properties": {
                    "requiredProperty": {
                      "types": ["string"],
                      "exampleSetFlag": false
                    }
                  },
                  "exampleSetFlag": false
                }
            """.trimIndent()
            }
        result = initial<JakartaNotEmptyWithAllNullProperties>()
            .analyzeTypeUsingReflection()
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
                    "requiredProperty"
                  ],
                  "types": ["object"],
                  "properties": {
                    "requiredProperty": {
                      "types": ["string"],
                      "exampleSetFlag": false
                    }
                  },
                  "exampleSetFlag": false
                }
            """.trimIndent()
            }
        result = initial<JakartaNotBlankWithAllNullProperties>()
            .analyzeTypeUsingReflection()
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
                    "requiredProperty"
                  ],
                  "types": ["object"],
                  "properties": {
                    "requiredProperty": {
                      "types": ["string"],
                      "exampleSetFlag": false
                    }
                  },
                  "exampleSetFlag": false
                }
            """.trimIndent()
            }
        result = initial<AllRequiredValidations>()
            .analyzeTypeUsingReflection()
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
                    "requiredProperty"
                  ],
                  "types": ["object"],
                  "properties": {
                    "requiredProperty": {
                      "types": ["string"],
                      "exampleSetFlag": false
                    }
                  },
                  "exampleSetFlag": false
                }
            """.trimIndent()
            }
        result = initial<JakartaAllRequiredValidations>()
            .analyzeTypeUsingReflection()
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
                    "requiredProperty"
                  ],
                  "types": ["object"],
                  "properties": {
                    "requiredProperty": {
                      "types": ["string"],
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
                example = "5",
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

            @field:Schema
            val emptyAnnotation: String,

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

    private class NotNullWithAllNullProperties(
        @field:NotNull
        val requiredProperty: String?
    )

    private class NotEmptyWithAllNullProperties(
        @field:NotEmpty
        val requiredProperty: String?
    )

    private class NotBlankWithAllNullProperties(
        @field:NotBlank
        val requiredProperty: String?
    )

    private class JakartaNotNullWithAllNullProperties(
        @field:jakarta.validation.constraints.NotNull
        val requiredProperty: String?
    )

    private class JakartaNotEmptyWithAllNullProperties(
        @field:jakarta.validation.constraints.NotEmpty
        val requiredProperty: String?
    )

    private class JakartaNotBlankWithAllNullProperties(
        @field:jakarta.validation.constraints.NotBlank
        val requiredProperty: String?
    )

    private class AllRequiredValidations(
        @field:NotNull
        @field:NotEmpty
        @field:NotBlank
        val requiredProperty: String?
    )

    private class JakartaAllRequiredValidations(
        @field:jakarta.validation.constraints.NotNull
        @field:jakarta.validation.constraints.NotEmpty
        @field:jakarta.validation.constraints.NotBlank
        val requiredProperty: String?
    )

}