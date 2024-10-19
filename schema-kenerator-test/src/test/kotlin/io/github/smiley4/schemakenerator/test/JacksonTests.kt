package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonIgnoreType
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.jackson.handleJacksonAnnotations
import io.github.smiley4.schemakenerator.jackson.jsonschema.handleJacksonJsonSchemaAnnotations
import io.github.smiley4.schemakenerator.jackson.swagger.handleJacksonSwaggerAnnotations
import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.reflect.typeOf

class JacksonTests : StringSpec({

    "@JsonSubTypes" {
        println("See ${SubAndSuperTypesTest::class.simpleName}")
    }

    "@JsonIgnore" {
        val result = typeOf<JsonIgnoreTestClass>()
            .processReflection()
            .handleJacksonAnnotations()
        (result.data as ObjectTypeData).members.also { members ->
            members shouldHaveSize 1
            members.first().name shouldBe "someValue"
        }
    }

    "@JsonIgnoreType" {
        val result = typeOf<JsonIgnoreTypeTestClass>()
            .processReflection()
            .handleJacksonAnnotations()
        (result.data as ObjectTypeData).members.also { members ->
            members shouldHaveSize 1
            members.first().name shouldBe "someValue"
        }
    }

    "@JsonIgnoreProperties" {
        val result = typeOf<JsonIgnorePropertiesTestClass>()
            .processReflection()
            .handleJacksonAnnotations()
        (result.data as ObjectTypeData).members.also { members ->
            members shouldHaveSize 1
            members.first().name shouldBe "someValue"
        }
    }

    "@JsonProperty" {
        val result = typeOf<JsonPropertyTestClass>()
            .processReflection()
            .handleJacksonAnnotations()
        (result.data as ObjectTypeData).members.also { members ->
            members shouldHaveSize 1
            members.first().name shouldBe "someValue"
            members.first().nullable shouldBe false
        }
    }

    "@JsonPropertyDescription - json-schema" {
        val result = typeOf<JsonPropertyDescriptionTestClass>()
            .processReflection()
            .generateJsonSchema()
            .handleJacksonJsonSchemaAnnotations()
            .compileInlining()

        result.json.shouldEqualJson(
            """
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
        )
    }

    "@JsonPropertyDescription - swagger" {
        val result = typeOf<JsonPropertyDescriptionTestClass>()
            .processReflection()
            .generateSwaggerSchema()
            .handleJacksonSwaggerAnnotations()
            .compileInlining()

        result.swagger.shouldEqualJson {
            """
                {
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
            """.trimIndent()
        }
    }

}) {

    companion object {

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

}