package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.FreeSpec
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

class RedirectTests : FreeSpec({

    "recursive redirects" - {

        "reflection" {
            val result = typeOf<TestClass>()
                .processReflection {
                    redirect<NestedClass, String>()
                    redirect<String, Int>()
                }
                .generateJsonSchema()
                .compileInlining()
                .json
                .prettyPrint()

            result.shouldEqualJson("""
                {
                   "type": "object",
                   "required": [
                      "data"
                   ],
                   "properties": {
                      "data": {
                         "type": "integer",
                         "minimum": -2147483648,
                         "maximum": 2147483647
                      }
                   }
                }
            """.trimIndent())
        }

        "kotlinx-serialization" {
            val result = typeOf<TestClass>()
                .processKotlinxSerialization {
                    redirect<NestedClass, String>()
                    redirect<String, Int>()
                }
                .generateJsonSchema()
                .compileInlining()
                .json
                .prettyPrint()

            result.shouldEqualJson("""
                {
                   "type": "object",
                   "required": [
                      "data"
                   ],
                   "properties": {
                      "data": {
                         "type": "integer",
                         "minimum": -2147483648,
                         "maximum": 2147483647
                      }
                   }
                }
            """.trimIndent())
        }

    }

    "redirect to nullable" - {

        "reflection" {
            val result = typeOf<TestClass>()
                .processReflection {
                    redirect<String, String?>()
                }
                .generateJsonSchema()
                .compileInlining()
                .json
                .prettyPrint()

            result.shouldEqualJson("""
                {
                  "type": "object",
                  "required": [
                    "data"
                  ],
                  "properties": {
                    "data": {
                      "type": "object",
                      "required": [
                        "someNumber"
                      ],
                      "properties": {
                        "someNumber": {
                          "type": "integer",
                          "minimum": -2147483648,
                          "maximum": 2147483647
                        },
                        "someText": {
                          "type": "string"
                        }
                      }
                    }
                  }
                }
            """.trimIndent())
        }

        "kotlinx-serialization" {
            val result = typeOf<TestClass>()
                .processKotlinxSerialization {
                    redirect<String, String?>()
                }
                .generateJsonSchema()
                .compileInlining()
                .json
                .prettyPrint()

            result.shouldEqualJson("""
                {
                  "type": "object",
                  "required": [
                    "data"
                  ],
                  "properties": {
                    "data": {
                      "type": "object",
                      "required": [
                        "someNumber"
                      ],
                      "properties": {
                        "someNumber": {
                          "type": "integer",
                          "minimum": -2147483648,
                          "maximum": 2147483647
                        },
                        "someText": {
                          "type": "string"
                        }
                      }
                    }
                  }
                }
            """.trimIndent())
        }

    }

}) {

    companion object {

        @Serializable
        class TestClass(
            val data: NestedClass
        )


        @Serializable
        class NestedClass(
            val someText: String,
            val someNumber: Int
        )

    }

}