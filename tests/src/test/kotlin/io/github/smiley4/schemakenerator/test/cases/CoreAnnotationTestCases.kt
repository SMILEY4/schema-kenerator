package io.github.smiley4.schemakenerator.test.cases

import io.github.smiley4.schemakenerator.core.annotations.Default
import io.github.smiley4.schemakenerator.core.annotations.Deprecated
import io.github.smiley4.schemakenerator.core.annotations.Description
import io.github.smiley4.schemakenerator.core.annotations.Example
import io.github.smiley4.schemakenerator.core.annotations.ExclusiveMax
import io.github.smiley4.schemakenerator.core.annotations.ExclusiveMin
import io.github.smiley4.schemakenerator.core.annotations.Format
import io.github.smiley4.schemakenerator.core.annotations.Max
import io.github.smiley4.schemakenerator.core.annotations.MaxLength
import io.github.smiley4.schemakenerator.core.annotations.Min
import io.github.smiley4.schemakenerator.core.annotations.MinLength
import io.github.smiley4.schemakenerator.core.annotations.Pattern
import io.github.smiley4.schemakenerator.core.annotations.Title
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.handleCoreAnnotations
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.handleCoreAnnotations
import kotlinx.serialization.Serializable
import org.checkerframework.common.value.qual.MinLen
import kotlin.reflect.typeOf

object CoreAnnotationTestCases {

    val basics = case("core annotations", "basic core annotations") {
        type = typeOf<CoreAnnotatedClass>()
        postGenerateSwaggerSchema = {
            this.handleCoreAnnotations()
        }
        postGenerateJsonSchema = {
            this.handleCoreAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "title": "Annotated Class",
                  "required": [
                      "stringValue",
                      "intValue",
                      "constrainedNumber",
                       "constrainedString",
                        "constrainedList"
                  ],
                  "type": "object",
                  "properties": {
                      "stringValue": {
                          "type": "string",
                          "description": "String field description",
                          "default": "A default String value",
                          "example": "An example of a String value",
                          "format": "text"
                      },
                      "intValue": {
                          "type": "integer",
                          "format": "int32",
                          "description": "Int field description",
                          "default": "1111",
                          "example": "2222"
                      },
                      "constrainedNumber" : {
                        "type" : "integer",
                        "format" : "int32",
                        "exclusiveMaximum" : 7,
                        "minimum" : 2
                      },
                      "constrainedString" : {
                        "type" : "string",
                        "maxLength" : 5,
                        "minLength" : 2,
                        "pattern" : "[0-9]*"
                      },
                      "constrainedList" : {
                        "type" : "array",
                        "items" : {
                          "type" : "boolean"
                        },
                        "maxItems" : 8,
                        "minItems" : 4
                      }
                  },
                  "description": "some description",
                  "deprecated": true,
                  "example": "example 1",
                  "default": "default value",
                  "format": "pair"
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
              "type": "object",
              "required": ["stringValue", "intValue", "constrainedNumber", "constrainedString", "constrainedList"],
              "properties": {
                  "stringValue": {
                      "type": "string",
                      "description": "String field description",
                      "default": "A default String value",
                      "examples": [
                          "An example of a String value"
                      ],
                      "format": "text"
                  },
                  "intValue": {
                      "type": "integer",
                      "default": "1111",
                      "minimum": -2147483648,
                      "maximum": 2147483647,
                      "description": "Int field description",
                      "examples": [
                          "2222"
                      ],
                      "format": "int32"
                  },
                  "constrainedNumber": {
                     "type": "integer",
                     "minimum": 2,
                     "exclusiveMaximum": 7
                  },
                  "constrainedString": {
                     "type": "string",
                     "minLength": 2,
                     "maxLength": 5,
                     "pattern": "[0-9]*"
                  },
                  "constrainedList": {
                     "type": "array",
                     "items": {
                        "type": "boolean"
                     },
                     "minItems": 4,
                     "maxItems": 8
                  }
              },
              "title": "Annotated Class",
              "description": "some description",
              "default": "default value",
              "examples": [
                  "example 1"
              ],
              "deprecated": true,
              "format": "pair"
            }
            """.trimIndent()
    }

    val annotatedValueClass = case("core annotations", "annotated value class") {
        type = typeOf<ClassWithAnnotatedValueClass>()
        postGenerateSwaggerSchema = {
            this.handleCoreAnnotations()
        }
        postGenerateJsonSchema = {
            this.handleCoreAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "myValue" : {
                      "type" : "string",
                      "format" : "pair",
                      "default" : "default value",
                      "deprecated" : true,
                      "description" : "some description",
                      "example" : "example 1",
                      "title" : "Annotated Class"
                    },
                    "myValueAnnotated" : {
                      "type" : "string",
                      "format" : "text",
                      "default" : "A default String value",
                      "deprecated" : true,
                      "description" : "String field description",
                      "example" : "An example of a String value",
                      "title" : "Annotated Class"
                    }
                  },
                  "required" : [ "myValue", "myValueAnnotated" ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
               "type": "object",
               "required": [
                  "myValue",
                  "myValueAnnotated"
               ],
               "properties": {
                 "myValue" : {
                   "type" : "string",
                   "format" : "pair",
                   "default" : "default value",
                   "deprecated" : true,
                   "description" : "some description",
                   "examples" : ["example 1"],
                   "title" : "Annotated Class"
                 },
                 "myValueAnnotated" : {
                   "type" : "string",
                   "format" : "text",
                   "default" : "A default String value",
                   "deprecated" : true,
                   "description" : "String field description",
                   "examples" : ["example 1", "An example of a String value"],
                   "title" : "Annotated Class"
                 }
               }
            }
        """.trimIndent()
    }


    @Title("Annotated Class")
    @Description("some description")
    @Default("default value")
    @Example("example 1")
    @Format("pair")
    @Deprecated
    @Serializable
    private class CoreAnnotatedClass(
        @Description("String field description")
        @Default("A default String value")
        @Example("An example of a String value")
        @Format("text")
        val stringValue: String,

        @Description("Int field description")
        @Default("1111")
        @Example("2222")
        @Format("int32")
        val intValue: Int,

        @Min(2)
        @Max(5)
        @ExclusiveMax(7)
        val constrainedNumber: Int,

        @MinLength(2)
        @MaxLength(5)
        @Pattern("[0-9]*")
        val constrainedString: String,

        @MinLength(4)
        @MaxLength(8)
        val constrainedList: List<Boolean>,
    )


    @Title("Annotated Class")
    @Description("some description")
    @Default("default value")
    @Example("example 1")
    @Format("pair")
    @Deprecated
    @Serializable
    @JvmInline
    private value class AnnotatedValueClass(val inlinedValue: String)


    @Serializable
    private data class ClassWithAnnotatedValueClass(
        @Description("String field description")
        @Default("A default String value")
        @Example("An example of a String value")
        @Format("text")
        val myValueAnnotated: AnnotatedValueClass,
        val myValue: AnnotatedValueClass,
    )

}
