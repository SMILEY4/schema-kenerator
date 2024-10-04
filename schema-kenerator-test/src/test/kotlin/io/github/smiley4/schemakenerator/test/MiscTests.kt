@file:OptIn(ExperimentalSerializationApi::class)

package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.annotations.Required
import io.github.smiley4.schemakenerator.core.connectSubTypes
import io.github.smiley4.schemakenerator.jackson.handleJacksonAnnotations
import io.github.smiley4.schemakenerator.jsonschema.OptionalHandling
import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.handleCoreAnnotations
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleCoreAnnotations
import io.github.smiley4.schemakenerator.test.Test.Companion.Parent
import io.github.smiley4.schemakenerator.validation.swagger.handleJavaxValidationAnnotations
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.FreeSpec
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import java.util.Optional
import javax.validation.constraints.Size
import kotlin.reflect.typeOf

class MiscTests : FreeSpec({

    "https://github.com/SMILEY4/schema-kenerator/issues/14 - redirect to nullable types" - {

        "reflection" {
            val result = typeOf<TestClassIssue14a>()
                .processReflection {
                    redirect<Optional<String?>, String?>()
                }
                .generateJsonSchema()
                .compileInlining()

            result.json.prettyPrint().shouldEqualJson(
                """
                {
                  "type": "object",
                  "required": [],
                  "properties": {
                    "name": {
                      "type": "string"
                    }
                  }
                }
            """.trimIndent()
            )
        }

        "kotlinx-serialization" {
            val result = typeOf<TestClassIssue14b>()
                .processKotlinxSerialization {
                    redirect<Int, String?>()
                }
                .generateJsonSchema()
                .compileInlining()

            result.json.prettyPrint().shouldEqualJson(
                """
                {
                  "type": "object",
                  "required": [],
                  "properties": {
                    "name": {
                      "type": "string"
                    }
                  }
                }
            """.trimIndent()
            )
        }

    }

    "https://github.com/SMILEY4/schema-kenerator/issues/16 - field nullability handling" - {

        "reflection" {
            val result = typeOf<TestClassIssue16>()
                .processReflection()
                .generateJsonSchema {
                    optionalHandling = OptionalHandling.NON_REQUIRED
                }
                .compileInlining()

            result.json.prettyPrint().shouldEqualJson(
                """
                {
                  "type": "object",
                  "required": [
                    "name"
                  ],
                  "properties": {
                    "description": {
                      "type": "string"
                    },
                    "name": {
                      "type": "string"
                    }
                  }
                }
            """.trimIndent()
            )
        }

        "kotlinx-serialization" {
            val result = typeOf<TestClassIssue16>()
                .processKotlinxSerialization()
                .generateJsonSchema {
                    optionalHandling = OptionalHandling.NON_REQUIRED
                }
                .compileInlining()

            result.json.prettyPrint().shouldEqualJson(
                """
                {
                  "type": "object",
                  "required": [
                    "name"
                  ],
                  "properties": {
                    "name": {
                      "type": "string"
                    },
                    "description": {
                      "type": "string"
                    }
                  }
                }
            """.trimIndent()
            )
        }

    }


    "https://github.com/SMILEY4/schema-kenerator/issues/19 - required annotation not working when all props nullable or optional" - {

        "json" {
            val result = typeOf<TestClassIssue19>()
                .processKotlinxSerialization()
                .generateJsonSchema()
                .handleCoreAnnotations()
                .compileInlining()

            result.json.prettyPrint().shouldEqualJson(
                """
                {
                  "type": "object",
                  "required": [
                    "prop1"
                  ],
                  "properties": {
                    "prop1": {
                      "type": "string"
                    },
                    "prop2": {
                      "type": "string"
                    }
                  }
                }
            """.trimIndent()
            )
        }

        "swagger" {
            val result = typeOf<TestClassIssue19>()
                .processKotlinxSerialization()
                .generateSwaggerSchema()
                .handleCoreAnnotations()
                .compileInlining()

            json.writeValueAsString(result.swagger).shouldEqualJson(
                """
                {
                  "required": [
                    "prop1"
                  ],
                  "type": "object",
                  "properties": {
                    "prop1": {
                      "types": ["string", "null"],
                      "exampleSetFlag": false
                    },
                    "prop2": {
                      "types": ["string", "null"],
                      "exampleSetFlag": false
                    }
                  },
                  "exampleSetFlag": false
                }
            """.trimIndent()
            )
        }

    }

    "https://github.com/SMILEY4/schema-kenerator/issues/20 - include annotations from constructor parameters" {
        val result = typeOf<TestClassIssue20>()
            .processReflection()
            .handleJacksonAnnotations()
            .generateSwaggerSchema()
            .handleJavaxValidationAnnotations()
            .compileInlining()

        json.writeValueAsString(result.swagger).shouldEqualJson(
            """
                {
                  "required": [ "passwordRenamed", "usernameRenamed" ],
                  "type": "object",
                  "properties": {
                    "passwordRenamed": {
                      "maxLength": 200,
                      "type": "string",
                      "exampleSetFlag": false
                    },
                    "usernameRenamed": {
                      "maxLength": 100,
                      "type": "string",
                      "exampleSetFlag": false
                    }
                  },
                  "exampleSetFlag": false
                }
            """.trimIndent()
        )
    }

    "https://github.com/SMILEY4/schema-kenerator/issues/3 - include discriminator field from kotlinx @JsonClassDiscriminator" {
        val result = typeOf<TestClassIssue3>()
            .processKotlinxSerialization()
            .connectSubTypes()
            .generateJsonSchema()
            .compileInlining()
        result.json.prettyPrint().shouldEqualJson("""
            {
              "anyOf": [
                {
                  "type": "object",
                  "required": [
                    "the_type",
                    "common",
                    "text"
                  ],
                  "properties": {
                    "the_type": {
                      "type": "string"
                    },
                    "common": {
                      "type": "boolean"
                    },
                    "text": {
                      "type": "string"
                    }
                  }
                },
                {
                  "type": "object",
                  "required": [
                    "the_type",
                    "common",
                    "number"
                  ],
                  "properties": {
                    "the_type": {
                      "type": "string"
                    },
                    "common": {
                      "type": "boolean"
                    },
                    "number": {
                      "type": "integer",
                      "minimum": -2147483648,
                      "maximum": 2147483647
                    }
                  }
                }
              ]
            }
        """.trimIndent())
    }

}) {

    companion object {

        private val json = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()!!


        class TestClassIssue14a(
            val name: Optional<String?>
        )


        @Serializable
        class TestClassIssue14b(
            val name: Int
        )


        @Serializable
        data class TestClassIssue16(
            val name: String,
            val description: String? = null
        )


        @Serializable
        data class TestClassIssue19(
            @Required
            val prop1: String?,
            val prop2: String? = null
        )

        data class TestClassIssue20(
            @field:Size(max = 100)
            @JsonProperty("usernameRenamed", required = true)
            val username: String?,

            @field:Size(max = 200)
            @JsonProperty("passwordRenamed", required = true)
            val password: String?
        )


        @Serializable
        @JsonClassDiscriminator("the_type")
        sealed class TestClassIssue3(val common: Boolean) {

            @Serializable
            @SerialName("child_one")
            data class ChildOne(val text: String) : TestClassIssue3(false)


            @Serializable
            @SerialName("child_two")
            data class ChildTwo(val number: Int) : TestClassIssue3(false)

        }

    }

}