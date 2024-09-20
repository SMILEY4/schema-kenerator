package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.jsonschema.OptionalHandling
import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.FreeSpec
import kotlinx.serialization.Serializable
import java.util.Optional
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

            result.json.prettyPrint().shouldEqualJson("""
                {
                  "type": "object",
                  "required": [],
                  "properties": {
                    "name": {
                      "type": "string"
                    }
                  }
                }
            """.trimIndent())
        }

        "kotlinx-serialization" {
            val result = typeOf<TestClassIssue14b>()
                .processKotlinxSerialization {
                    redirect<Int, String?>()
                }
                .generateJsonSchema()
                .compileInlining()

            result.json.prettyPrint().shouldEqualJson("""
                {
                  "type": "object",
                  "required": [],
                  "properties": {
                    "name": {
                      "type": "string"
                    }
                  }
                }
            """.trimIndent())
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

            result.json.prettyPrint().shouldEqualJson("""
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
            """.trimIndent())
        }

        "kotlinx-serialization" {

            val result = typeOf<TestClassIssue16>()
                .processKotlinxSerialization()
                .generateJsonSchema {
                    optionalHandling = OptionalHandling.NON_REQUIRED
                }
                .compileInlining()

            result.json.prettyPrint().shouldEqualJson("""
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
            """.trimIndent())
        }

    }

}) {

    companion object {

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

    }

}