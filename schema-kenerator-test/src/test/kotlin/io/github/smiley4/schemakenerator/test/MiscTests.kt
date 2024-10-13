@file:OptIn(ExperimentalSerializationApi::class)

package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.annotations.Required
import io.github.smiley4.schemakenerator.core.renameProperties
import io.github.smiley4.schemakenerator.jackson.handleJacksonAnnotations
import io.github.smiley4.schemakenerator.jsonschema.OptionalHandling
import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.customizeProperties
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.handleCoreAnnotations
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.github.smiley4.schemakenerator.serialization.renameProperties
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleCoreAnnotations
import io.github.smiley4.schemakenerator.validation.swagger.handleJavaxValidationAnnotations
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.FreeSpec
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNamingStrategy
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

            result.json.prettyPrint().shouldEqualLenient(
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

            result.json.prettyPrint().shouldEqualLenient(
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

            result.json.prettyPrint().shouldEqualLenient(
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

            result.json.prettyPrint().shouldEqualLenient(
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

            result.json.prettyPrint().shouldEqualLenient(
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

            json.writeValueAsString(result.swagger).shouldEqualLenient(
                """
                {
                  "required": [
                    "prop1"
                  ],
                  "types": ["object"],
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

        json.writeValueAsString(result.swagger).shouldEqualLenient(
            """
                {
                  "required": [ "passwordRenamed", "usernameRenamed" ],
                  "types": ["object"],
                  "properties": {
                    "passwordRenamed": {
                      "maxLength": 200,
                      "types": ["string"],
                      "exampleSetFlag": false
                    },
                    "usernameRenamed": {
                      "maxLength": 100,
                      "types": ["string"],
                      "exampleSetFlag": false
                    }
                  },
                  "exampleSetFlag": false
                }
            """.trimIndent()
        )
    }

    "https://github.com/SMILEY4/schema-kenerator/issues/18 - support renaming properties"- {

        "custom renameing (adding prefix)" {
            val result = typeOf<TestClassIssue18>()
                .processKotlinxSerialization()
                .renameProperties { name -> "prefix_$name" }
                .generateSwaggerSchema()
                .handleCoreAnnotations()
                .compileInlining()

            json.writeValueAsString(result.swagger).shouldEqualLenient(
                """
                {
                  "required": [ "prefix_nameOfPerson", "prefix_numberOfYears" ],
                  "types": ["object"],
                  "properties": {
                    "prefix_nameOfPerson": {
                      "types": ["string"],
                      "exampleSetFlag": false
                    },
                    "prefix_numberOfYears": {
                      "types": ["integer"],
                      "format": "int32",
                      "exampleSetFlag": false
                    }
                  },
                  "exampleSetFlag": false
                }
            """.trimIndent()
            )
        }

        "kotlinx naming strategy (snake case)" {
            val result = typeOf<TestClassIssue18>()
                .processKotlinxSerialization()
                .renameProperties(JsonNamingStrategy.SnakeCase)
                .generateSwaggerSchema()
                .handleCoreAnnotations()
                .compileInlining()

            json.writeValueAsString(result.swagger).shouldEqualLenient(
                """
                {
                  "required": [ "name_of_person", "number_of_years" ],
                  "types": ["object"],
                  "properties": {
                    "name_of_person": {
                      "types": ["string"],
                      "exampleSetFlag": false
                    },
                    "number_of_years": {
                      "types": ["integer"],
                      "format": "int32",
                      "exampleSetFlag": false
                    }
                  },
                  "exampleSetFlag": false
                }
            """.trimIndent()
            )
        }

    }

    "customize property with shared type results in output schema with both all properties being modified" {

        class TestClass(
            val describeMe: String,
            val otherProperty: String
        )

        val jsonSchema = typeOf<TestClass>()
            .processReflection()
            .generateJsonSchema()
            .customizeProperties { propertyData, propertySchema ->
                if(propertyData.name == "describeMe" && propertySchema is JsonObject) {
                    propertySchema.properties["description"] = JsonTextValue("test description")
                }
            }
            .compileInlining()
            .json.prettyPrint()

        jsonSchema shouldEqualJson """
            {
               "type": "object",
               "required": [
                  "describeMe",
                  "otherProperty"
               ],
               "properties": {
                  "describeMe": {
                     "type": "string",
                     "description": "test description"
                  },
                  "otherProperty": {
                     "type": "string"
                  }
               }
            }
        """.trimIndent()

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
        data class TestClassIssue18(
            val nameOfPerson: String,
            val numberOfYears: Int
        )

    }

}