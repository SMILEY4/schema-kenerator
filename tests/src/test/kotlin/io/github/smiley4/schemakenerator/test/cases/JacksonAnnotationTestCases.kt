package io.github.smiley4.schemakenerator.test.cases

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonIgnoreType
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import io.github.smiley4.schemakenerator.jackson.JacksonSteps.handleJacksonAnnotations
import io.github.smiley4.schemakenerator.jackson.jsonschema.JacksonJsonSchemaSteps.handleJacksonJsonSchemaAnnotations
import io.github.smiley4.schemakenerator.jackson.swagger.JacksonSwaggerSteps.handleJacksonSwaggerAnnotations
import kotlin.reflect.typeOf

object JacksonAnnotationTestCases {

    val jsonIgnore = case("jackson annotations", "jackson @JsonIgnore") {
        type = typeOf<JsonIgnoreTestClass>()
        withKotlinxSerialization = false
        postAnalyze = {
            this.handleJacksonAnnotations()
        }
        postGenerateSwaggerSchema = {
            this.handleJacksonSwaggerAnnotations()
        }
        postGenerateJsonSchema = {
            this.handleJacksonJsonSchemaAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "someValue": {
                      "type": "boolean"
                    }
                  },
                  "required": [
                    "someValue"
                  ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
              "type": "object",
              "required": [
                "someValue"
              ],
              "properties": {
                "someValue": {
                  "type": "boolean"
                }
              }
            }
            """.trimIndent()
    }

    val jsonIgnoreType = case("jackson annotations", "jackson @JsonIgnoreType") {
        type = typeOf<JsonIgnoreTypeTestClass>()
        withKotlinxSerialization = false
        postAnalyze = {
            this.handleJacksonAnnotations()
        }
        postGenerateSwaggerSchema = {
            this.handleJacksonSwaggerAnnotations()
        }
        postGenerateJsonSchema = {
            this.handleJacksonJsonSchemaAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "someValue": {
                      "type": "boolean"
                    }
                  },
                  "required": [
                    "someValue"
                  ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
              "type": "object",
              "required": [
                "someValue"
              ],
              "properties": {
                "someValue": {
                  "type": "boolean"
                }
              }
            }
            """.trimIndent()
    }

    val jsonIgnoreProperties = case("jackson annotations", "jackson @JsonIgnoreProperties") {
        type = typeOf<JsonIgnorePropertiesTestClass>()
        withKotlinxSerialization = false
        postAnalyze = {
            this.handleJacksonAnnotations()
        }
        postGenerateSwaggerSchema = {
            this.handleJacksonSwaggerAnnotations()
        }
        postGenerateJsonSchema = {
            this.handleJacksonJsonSchemaAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "someValue": {
                      "type": "boolean"
                    }
                  },
                  "required": [
                    "someValue"
                  ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
              "type": "object",
              "required": [
                "someValue"
              ],
              "properties": {
                "someValue": {
                  "type": "boolean"
                }
              }
            }
            """.trimIndent()
    }

    val jsonProperty = case("jackson annotations", "jackson @JsonProperty") {
        type = typeOf<JsonPropertyTestClass>()
        withKotlinxSerialization = false
        postAnalyze = {
            this.handleJacksonAnnotations()
        }
        postGenerateSwaggerSchema = {
            this.handleJacksonSwaggerAnnotations()
        }
        postGenerateJsonSchema = {
            this.handleJacksonJsonSchemaAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "someValue": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "someValue"
                  ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
              "type": "object",
              "required": [
                "someValue"
              ],
              "properties": {
                "someValue": {
                  "type": "string"
                }
              }
            }
            """.trimIndent()
    }

    val jsonPropertyDescription = case("jackson annotations", "jackson @JsonPropertyDescription") {
        type = typeOf<JsonPropertyDescriptionTestClass>()
        withKotlinxSerialization = false
        postAnalyze = {
            this.handleJacksonAnnotations()
        }
        postGenerateSwaggerSchema = {
            this.handleJacksonSwaggerAnnotations()
        }
        postGenerateJsonSchema = {
            this.handleJacksonJsonSchemaAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "someValue": {
                      "type": "string",
                      "description": "Jackson property description"
                    }
                  },
                  "required": [
                    "someValue"
                  ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
             {
              "type": "object",
              "required": [
                "someValue"
              ],
              "properties": {
                "someValue": {
                  "type": "string",
                  "description": "Jackson property description"
                }
              }
            }
            """.trimIndent()
    }

    private class JsonPropertyDescriptionTestClass(
        @field:JsonPropertyDescription("Jackson property description")
        val someValue: String
    )

    private class JsonIgnoreTestClass(
        @JsonIgnore
        val ignoredValue: String,
        val someValue: Boolean,
    )

    private class JsonIgnoreTypeTestClass(
        val ignoredValue: IgnoredType,
        val someValue: Boolean,
    )


    @JsonIgnoreType
    private class IgnoredType(val myNumber: Int)


    @JsonIgnoreProperties("ignoredValue", "anotherIgnoredValue", "someUnknownValue")
    private class JsonIgnorePropertiesTestClass(
        val ignoredValue: String,
        val anotherIgnoredValue: Int,
        val someValue: Boolean,
    )

    private class JsonPropertyTestClass(
        @field:JsonProperty("someValue", required = true)
        val myValue: String?,
    )

}
