package io.github.smiley4.schemakenerator.test.cases

import kotlinx.serialization.Serializable
import java.util.Optional
import kotlin.reflect.typeOf
import io.github.smiley4.schemakenerator.reflection.data.TypeRedirect as ReflectionTypeRedirect
import io.github.smiley4.schemakenerator.serialization.data.TypeRedirect as SerializationTypeRedirect

object RedirectTypeTestCases {

    val matchFromKeepTo = case("type redirects", "match 'from nullability', keep 'to nullability'") {
        type = typeOf<SimpleTestClass>()
        reflectionConfig = {
            redirect {
                from<String>(ReflectionTypeRedirect.FromNullability.MATCH)
                to<Int?>(ReflectionTypeRedirect.ToNullability.KEEP)
            }
        }
        kotlinxSerializationConfig = {
            redirect {
                from<String>(SerializationTypeRedirect.FromNullability.MATCH)
                to<Int?>(SerializationTypeRedirect.ToNullability.KEEP)
            }
        }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "nullableText" : {
                      "type" : [ "null", "string" ]
                    },
                    "requiredText" : {
                      "type" : "integer",
                      "format" : "int32"
                    }
                  },
                  "required" : [ "requiredText" ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
               "type": "object",
               "required": [
                  "requiredText"
               ],
               "properties": {
                  "nullableText": {
                     "type": "string"
                  },
                  "requiredText": {
                     "type": "integer",
                     "minimum": -2147483648,
                     "maximum": 2147483647
                  }
               }
            }
            """.trimIndent()
    }

    val matchFromReplaceTo = case("type redirects", "match 'from nullability', replace 'to nullability'") {
        type = typeOf<SimpleTestClass>()
        reflectionConfig = {
            redirect {
                from<String>(ReflectionTypeRedirect.FromNullability.MATCH)
                to<Int?>(ReflectionTypeRedirect.ToNullability.REPLACE)
            }
        }
        kotlinxSerializationConfig = {
            redirect {
                from<String>(SerializationTypeRedirect.FromNullability.MATCH)
                to<Int?>(SerializationTypeRedirect.ToNullability.REPLACE)
            }
        }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "nullableText" : {
                      "type" : [ "null", "string" ]
                    },
                    "requiredText" : {
                      "type" : [ "null", "integer" ],
                      "format" : "int32"
                    }
                  }
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
               "type": "object",
               "required": [],
               "properties": {
                  "nullableText": {
                     "type": "string"
                  },
                  "requiredText": {
                     "type": "integer",
                     "minimum": -2147483648,
                     "maximum": 2147483647
                  }
               }
            }
            """.trimIndent()
    }

    val ignoreFromKeepTo = case("type redirects", "ignore 'from nullability', match 'to nullability'") {
        type = typeOf<SimpleTestClass>()
        reflectionConfig = {
            redirect {
                from<String>(ReflectionTypeRedirect.FromNullability.IGNORE)
                to<Int?>(ReflectionTypeRedirect.ToNullability.KEEP)
            }
        }
        kotlinxSerializationConfig = {
            redirect {
                from<String>(SerializationTypeRedirect.FromNullability.IGNORE)
                to<Int?>(SerializationTypeRedirect.ToNullability.KEEP)
            }
        }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "nullableText" : {
                      "type" : [ "null", "integer" ],
                      "format" : "int32"
                    },
                    "requiredText" : {
                      "type" : "integer",
                      "format" : "int32"
                    }
                  },
                  "required" : [ "requiredText" ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
               "type": "object",
               "required": [
                  "requiredText"
               ],
               "properties": {
                  "nullableText": {
                     "type": "integer",
                     "minimum": -2147483648,
                     "maximum": 2147483647
                  },
                  "requiredText": {
                     "type": "integer",
                     "minimum": -2147483648,
                     "maximum": 2147483647
                  }
               }
            }
            """.trimIndent()
    }

    val ignoreFromReplaceTo = case("type redirects", "ignore 'from nullability', replace 'to nullability'") {
        type = typeOf<SimpleTestClass>()
        reflectionConfig = {
            redirect {
                from<String>(ReflectionTypeRedirect.FromNullability.IGNORE)
                to<Int?>(ReflectionTypeRedirect.ToNullability.REPLACE)
            }
        }
        kotlinxSerializationConfig = {
            redirect {
                from<String>(SerializationTypeRedirect.FromNullability.IGNORE)
                to<Int?>(SerializationTypeRedirect.ToNullability.REPLACE)
            }
        }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "nullableText" : {
                      "type" : [ "null", "integer" ],
                      "format" : "int32"
                    },
                    "requiredText" : {
                      "type" : [ "null", "integer" ],
                      "format" : "int32"
                    }
                  }
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
               "type": "object",
               "required": [],
               "properties": {
                  "nullableText": {
                     "type": "integer",
                     "minimum": -2147483648,
                     "maximum": 2147483647
                  },
                  "requiredText": {
                     "type": "integer",
                     "minimum": -2147483648,
                     "maximum": 2147483647
                  }
               }
            }
            """.trimIndent()
    }

    val chain = case("type redirects", "redirect chain (NestedClass -> String -> Int)") {
        type = typeOf<TestClass>()
        reflectionConfig = {
            redirect {
                from<NestedClass>()
                to<String>()
            }
            redirect {
                from<String>()
                to<Int>()
            }
        }
        kotlinxSerializationConfig = {
            redirect {
                from<NestedClass>()
                to<String>()
            }
            redirect {
                from<String>()
                to<Int>()
            }
        }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "data" : {
                      "type" : [ "null", "integer" ],
                      "format" : "int32"
                    }
                  }
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
               "type": "object",
               "required": [],
               "properties": {
                  "data": {
                     "type": "integer",
                     "minimum": -2147483648,
                     "maximum": 2147483647
                  }
               }
            }
            """.trimIndent()
    }

    val optional = case("type redirects", "optional handling") {
        type = typeOf<TestClassOptional>()
        withKotlinxSerialization = false
        reflectionConfig = {
            redirect {
                from<Optional<String?>>()
                to<String?>(ReflectionTypeRedirect.ToNullability.REPLACE)
            }
        }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "name" : {
                      "type" : [ "null", "string" ]
                    }
                  }
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
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
    }

    @Serializable
    private class SimpleTestClass(
        val requiredText: String,
        val nullableText: String?,
    )


    @Serializable
    private class TestClass(
        val data: NestedClass?
    )


    @Serializable
    private class NestedClass(
        val someText: String,
        val someNumber: Int
    )

    private class TestClassOptional(
        val name: Optional<String?>
    )

}