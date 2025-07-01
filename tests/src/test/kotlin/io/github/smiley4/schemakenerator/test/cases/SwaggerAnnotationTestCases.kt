package io.github.smiley4.schemakenerator.test.cases

import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.handleSchemaAnnotations
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import kotlin.reflect.typeOf

object SwaggerAnnotationTestCases {

    val basics = case("swagger annotations", "basics") {
        type = typeOf<MyTestClass>()
        withKotlinxSerialization = false
        postGenerateSwaggerSchema = {
            this.handleSchemaAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "description": "some description",
                  "properties": {
                    "emptyAnnotation": {
                      "type": "string"
                    },
                    "someTags": {
                      "type": "array",
                      "items": {
                        "type": "string"
                      },
                      "maxItems": 10,
                      "minItems": 0,
                      "uniqueItems": true
                    },
                    "someValue": {
                      "type": "integer",
                      "format": "single-digit",
                      "title": "Some Value",
                      "description": "some value",
                      "enum": [
                        "1",
                        "2",
                        "3",
                        "4"
                      ],
                      "default": "1",
                      "example": "5",
                      "minimum": 0,
                      "maximum": 9,
                      "minLength": 1,
                      "maxLength": 10,
                      "readOnly": true
                    }
                  },
                  "required": [
                    "emptyAnnotation",
                    "someTags"
                  ],
                  "title": "My Test Class"
                }
              }
            }
            """.trimIndent()
        expectedJson = null
    }

    val partiallySpecified = case("swagger annotations", "partially specified") {
        type = typeOf<PartiallySpecified>()
        withKotlinxSerialization = false
        postGenerateSwaggerSchema = {
            this.handleSchemaAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "x": {
                      "type": "string",
                      "description": "Mysterious thing"
                    }
                  },
                  "required": [
                    "x"
                  ]
                }
              }
            }
            """.trimIndent()
        expectedJson = null
    }

    val hiddenFields = case("swagger annotations", "hidden fields with no required fields") {
        type = typeOf<AllOptionalFields>()
        withKotlinxSerialization = false
        postGenerateSwaggerSchema = {
            this.handleSchemaAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "firstField": {
                      "type": [
                        "null",
                        "string"
                      ],
                      "description": "The first field"
                    }
                  }
                }
              }
            }
            """.trimIndent()
        expectedJson = null
    }

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

}
