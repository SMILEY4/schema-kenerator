package io.github.smiley4.schemakenerator.test.cases

import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.withTitle
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.withTitle
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf
import io.github.smiley4.schemakenerator.jsonschema.data.RefType as JsonRefType
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType as JsonTitleType
import io.github.smiley4.schemakenerator.swagger.data.RefType as SwaggerRefType
import io.github.smiley4.schemakenerator.swagger.data.TitleType as SwaggerTitleType

object TitleTestCases {

    val simple = case("title", "simple") {
        type = typeOf<TestClass>()
        swaggerRefType = SwaggerRefType.SIMPLE
        jsonRefType = JsonRefType.SIMPLE
        postGenerateSwaggerSchema = {
            this.withTitle(SwaggerTitleType.SIMPLE)
        }
        postGenerateJsonSchema = {
            this.withTitle(JsonTitleType.SIMPLE)
        }
        // language=json
        expectedSwaggerInline = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "nested" : {
                      "type" : "object",
                      "properties" : {
                        "someValue" : {
                          "type" : "object",
                          "properties" : {
                            "value1" : {
                              "type" : "string",
                              "title" : "String"
                            },
                            "value2" : {
                              "type" : "integer",
                              "format" : "int32",
                              "title" : "Int"
                            }
                          },
                          "required" : [ "value1", "value2" ],
                          "title" : "NestedClass1<String,Int>"
                        }
                      },
                      "required" : [ "someValue" ],
                      "title" : "NestedClass2<NestedClass1<String,Int>>"
                    }
                  },
                  "required" : [ "nested" ],
                  "title" : "TestClass"
                }
              }
            }
            """.trimIndent()
        expectedSwaggerInlineKotlinxSerialization = expectedSwaggerInline!!
            .replace("NestedClass2<NestedClass1<String,Int>>", "NestedClass2")
            .replace("NestedClass1<String,Int>", "NestedClass1")
        // language=json
        expectedSwaggerReference = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "nested" : {
                      "${'$'}ref" : "#/components/schemas/NestedClass2<NestedClass1<String,Int>>"
                    }
                  },
                  "required" : [ "nested" ],
                  "title" : "TestClass"
                },
                "NestedClass2<NestedClass1<String,Int>>" : {
                  "type" : "object",
                  "properties" : {
                    "someValue" : {
                      "${'$'}ref" : "#/components/schemas/NestedClass1<String,Int>"
                    }
                  },
                  "required" : [ "someValue" ],
                  "title" : "NestedClass2<NestedClass1<String,Int>>"
                },
                "NestedClass1<String,Int>" : {
                  "type" : "object",
                  "properties" : {
                    "value1" : {
                      "type" : "string",
                      "title" : "String"
                    },
                    "value2" : {
                      "type" : "integer",
                      "format" : "int32",
                      "title" : "Int"
                    }
                  },
                  "required" : [ "value1", "value2" ],
                  "title" : "NestedClass1<String,Int>"
                }
              }
            }
            """.trimIndent()
        expectedSwaggerReferenceKotlinxSerialization = expectedSwaggerReference!!
            .replace("NestedClass2<NestedClass1<String,Int>>", "NestedClass2")
            .replace("NestedClass1<String,Int>", "NestedClass1")
        // language=json
        expectedJsonInline = """
            {
               "type": "object",
               "required": [
                  "nested"
               ],
               "properties": {
                  "nested": {
                     "type": "object",
                     "required": [
                        "someValue"
                     ],
                     "properties": {
                        "someValue": {
                           "type": "object",
                           "required": [
                              "value1",
                              "value2"
                           ],
                           "properties": {
                              "value1": {
                                 "type": "string",
                                 "title": "String"
                              },
                              "value2": {
                                 "type": "integer",
                                 "minimum": -2147483648,
                                 "maximum": 2147483647,
                                 "title": "Int"
                              }
                           },
                           "title": "NestedClass1<String,Int>"
                        }
                     },
                     "title": "NestedClass2<NestedClass1<String,Int>>"
                  }
               },
               "title": "TestClass"
            }
            """.trimIndent()
        expectedJsonInlineKotlinxSerialization = expectedJsonInline!!
            .replace("NestedClass2<NestedClass1<String,Int>>", "NestedClass2")
            .replace("NestedClass1<String,Int>", "NestedClass1")
        // language=json
        expectedJsonReference = """
            {
               "type": "object",
               "required": [
                  "nested"
               ],
               "properties": {
                  "nested": {
                     "${'$'}ref": "#/${'$'}defs/NestedClass2<NestedClass1<String,Int>>"
                  }
               },
               "title": "TestClass",
               "${'$'}defs": {
                  "NestedClass2<NestedClass1<String,Int>>": {
                     "type": "object",
                     "required": [
                        "someValue"
                     ],
                     "properties": {
                        "someValue": {
                           "${'$'}ref": "#/${'$'}defs/NestedClass1<String,Int>"
                        }
                     },
                     "title": "NestedClass2<NestedClass1<String,Int>>"
                  },
                  "NestedClass1<String,Int>": {
                     "type": "object",
                     "required": [
                        "value1",
                        "value2"
                     ],
                     "properties": {
                        "value1": {
                           "type": "string",
                           "title": "String"
                        },
                        "value2": {
                           "type": "integer",
                           "minimum": -2147483648,
                           "maximum": 2147483647,
                           "title": "Int"
                        }
                     },
                     "title": "NestedClass1<String,Int>"
                  }
               }
            }
            """.trimIndent()
        expectedJsonReferenceKotlinxSerialization = expectedJsonReference!!
            .replace("NestedClass2<NestedClass1<String,Int>>", "NestedClass2")
            .replace("NestedClass1<String,Int>", "NestedClass1")
    }

    val full = case("title", "full") {
        type = typeOf<TestClass>()
        swaggerRefType = SwaggerRefType.FULL
        jsonRefType = JsonRefType.FULL
        postGenerateSwaggerSchema = {
            this.withTitle(SwaggerTitleType.FULL)
        }
        postGenerateJsonSchema = {
            this.withTitle(JsonTitleType.FULL)
        }
        // language=json
        expectedSwaggerInline = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "nested" : {
                      "type" : "object",
                      "properties" : {
                        "someValue" : {
                          "type" : "object",
                          "properties" : {
                            "value1" : {
                              "type" : "string",
                              "title" : "kotlin.String"
                            },
                            "value2" : {
                              "type" : "integer",
                              "format" : "int32",
                              "title" : "kotlin.Int"
                            }
                          },
                          "required" : [ "value1", "value2" ],
                          "title" : "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>"
                        }
                      },
                      "required" : [ "someValue" ],
                      "title" : "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass2<io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>>"
                    }
                  },
                  "required" : [ "nested" ],
                  "title" : "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.TestClass"
                }
              }
            }
            """.trimIndent()
        expectedSwaggerInlineKotlinxSerialization = expectedSwaggerInline!!
            .replace("NestedClass2<io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>>", "NestedClass2")
            .replace("NestedClass1<kotlin.String,kotlin.Int>", "NestedClass1")
        // language=json
        expectedSwaggerReference = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "nested" : {
                      "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass2<io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>>"
                    }
                  },
                  "required" : [ "nested" ],
                  "title" : "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.TestClass"
                },
                "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass2<io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>>": {
                  "type" : "object",
                  "properties" : {
                    "someValue" : {
                      "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>"
                    }
                  },
                  "required" : [ "someValue" ],
                  "title" : "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass2<io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>>"
                },
                "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>": {
                  "type" : "object",
                  "properties" : {
                    "value1" : {
                      "type" : "string",
                      "title" : "kotlin.String"
                    },
                    "value2" : {
                      "type" : "integer",
                      "format" : "int32",
                      "title" : "kotlin.Int"
                    }
                  },
                  "required" : [ "value1", "value2" ],
                  "title" : "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>"
                }
              }
            }
            """.trimIndent()
        expectedSwaggerReferenceKotlinxSerialization = expectedSwaggerReference!!
            .replace("NestedClass2<io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>>", "NestedClass2")
            .replace("NestedClass1<kotlin.String,kotlin.Int>", "NestedClass1")
        // language=json
        expectedJsonInline = """
            {
               "type": "object",
               "required": [
                  "nested"
               ],
               "properties": {
                  "nested": {
                     "type": "object",
                     "required": [
                        "someValue"
                     ],
                     "properties": {
                        "someValue": {
                           "type": "object",
                           "required": [
                              "value1",
                              "value2"
                           ],
                           "properties": {
                              "value1": {
                                 "type": "string",
                                 "title": "kotlin.String"
                              },
                              "value2": {
                                 "type": "integer",
                                 "minimum": -2147483648,
                                 "maximum": 2147483647,
                                 "title": "kotlin.Int"
                              }
                           },
                           "title": "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>"
                        }
                     },
                     "title": "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass2<io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>>"
                  }
               },
               "title": "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.TestClass"
            }
            """.trimIndent()
        expectedJsonInlineKotlinxSerialization = expectedJsonInline!!
            .replace("NestedClass2<io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>>", "NestedClass2")
            .replace("NestedClass1<kotlin.String,kotlin.Int>", "NestedClass1")
        // language=json
        expectedJsonReference = """
            {
               "type": "object",
               "required": [
                  "nested"
               ],
               "properties": {
                  "nested": {
                     "${'$'}ref": "#/${'$'}defs/io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass2<io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>>"
                  }
               },
               "title": "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.TestClass",
               "${'$'}defs": {
                  "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass2<io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>>": {
                     "type": "object",
                     "required": [
                        "someValue"
                     ],
                     "properties": {
                        "someValue": {
                          "${'$'}ref": "#/${'$'}defs/io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>"
                        }
                     },
                     "title": "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass2<io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>>"
                  },
                  "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>": {
                     "type": "object",
                     "required": [
                        "value1",
                        "value2"
                     ],
                     "properties": {
                        "value1": {
                           "type": "string",
                           "title": "kotlin.String"
                        },
                        "value2": {
                           "type": "integer",
                           "minimum": -2147483648,
                           "maximum": 2147483647,
                           "title": "kotlin.Int"
                        }
                     },
                     "title": "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>"
                  }
               }
            }
            """.trimIndent()
        expectedJsonReferenceKotlinxSerialization = expectedJsonReference!!
            .replace("NestedClass2<io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1<kotlin.String,kotlin.Int>>", "NestedClass2")
            .replace("NestedClass1<kotlin.String,kotlin.Int>", "NestedClass1")
    }

    val openApiSimple = case("title", "openapi simple") {
        type = typeOf<TestClass>()
        swaggerRefType = SwaggerRefType.OPENAPI_SIMPLE
        postGenerateSwaggerSchema = {
            this.withTitle(SwaggerTitleType.OPENAPI_SIMPLE)
        }
        // language=json
        expectedSwaggerInline = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "nested" : {
                      "type" : "object",
                      "properties" : {
                        "someValue" : {
                          "type" : "object",
                          "properties" : {
                            "value1" : {
                              "type" : "string",
                              "title" : "String"
                            },
                            "value2" : {
                              "type" : "integer",
                              "format" : "int32",
                              "title" : "Int"
                            }
                          },
                          "required" : [ "value1", "value2" ],
                          "title" : "NestedClass1_String-Int"
                        }
                      },
                      "required" : [ "someValue" ],
                      "title" : "NestedClass2_NestedClass1_String-Int"
                    }
                  },
                  "required" : [ "nested" ],
                  "title" : "TestClass"
                }
              }
            }
            """.trimIndent()
        expectedSwaggerInlineKotlinxSerialization = expectedSwaggerInline!!
            .replace("NestedClass1_String-Int", "NestedClass1")
            .replace("NestedClass2_NestedClass1_String-Int", "NestedClass2") // todo: correct ?
            .replace("NestedClass2_NestedClass1", "NestedClass2") // todo: correct ?
        // language=json
        expectedSwaggerReference = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "nested" : {
                      "${'$'}ref": "#/components/schemas/NestedClass2_NestedClass1_String-Int"
                    }
                  },
                  "required" : [ "nested" ],
                  "title" : "TestClass"
                },
                "NestedClass2_NestedClass1_String-Int": {
                  "type" : "object",
                  "properties" : {
                    "someValue" : {
                      "${'$'}ref" : "#/components/schemas/NestedClass1_String-Int"
                    }
                  },
                  "required" : [ "someValue" ],
                  "title" : "NestedClass2_NestedClass1_String-Int"
                },
                "NestedClass1_String-Int": {
                  "type" : "object",
                  "properties" : {
                    "value1" : {
                      "type" : "string",
                      "title" : "String"
                    },
                    "value2" : {
                      "type" : "integer",
                      "format" : "int32",
                      "title" : "Int"
                    }
                  },
                  "required" : [ "value1", "value2" ],
                  "title" : "NestedClass1_String-Int"
                }
              }
            }
            """.trimIndent()
        expectedSwaggerReferenceKotlinxSerialization = expectedSwaggerReference!!
            .replace("NestedClass1_String-Int", "NestedClass1")
            .replace("NestedClass2_NestedClass1_String-Int", "NestedClass2")
            .replace("NestedClass2_NestedClass1", "NestedClass2")

        expectedJson = null
    }

    val openApiFull = case("title", "openapi full") {
        type = typeOf<TestClass>()
        swaggerRefType = SwaggerRefType.OPENAPI_FULL
        postGenerateSwaggerSchema = {
            this.withTitle(SwaggerTitleType.OPENAPI_FULL)
        }
        // language=json
        expectedSwaggerInline = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "nested": {
                      "type": "object",
                      "properties": {
                        "someValue": {
                          "type": "object",
                          "properties": {
                            "value1": {
                              "type": "string",
                              "title": "kotlin.String"
                            },
                            "value2": {
                              "type": "integer",
                              "format": "int32",
                              "title": "kotlin.Int"
                            }
                          },
                          "required": [
                            "value1",
                            "value2"
                          ],
                          "title": "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1_kotlin.String-kotlin.Int"
                        }
                      },
                      "required": [
                        "someValue"
                      ],
                      "title": "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass2_io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1_kotlin.String-kotlin.Int"
                    }
                  },
                  "required": [
                    "nested"
                  ],
                  "title": "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.TestClass"
                }
              }
            }
            """.trimIndent()
        expectedSwaggerInlineKotlinxSerialization = expectedSwaggerInline!!
            .replace("NestedClass1_String-Int", "NestedClass1")
            .replace("NestedClass1_kotlin.String-kotlin.Int", "NestedClass1")
            .replace("NestedClass2_NestedClass1_String-Int", "NestedClass2")
            .replace("NestedClass2_NestedClass1", "NestedClass2")
            .replace("NestedClass2_io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1_kotlin.String-kotlin.Int", "NestedClass2")
            .replace("NestedClass2_io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1", "NestedClass2")

        // language=json
        expectedSwaggerReference = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "nested" : {
                      "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass2_io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1_kotlin.String-kotlin.Int"
                    }
                  },
                  "required" : [ "nested" ],
                  "title" : "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.TestClass"
                },
                "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass2_io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1_kotlin.String-kotlin.Int": {
                  "type" : "object",
                  "properties" : {
                    "someValue" : {
                      "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1_kotlin.String-kotlin.Int"
                    }
                  },
                  "required" : [ "someValue" ],
                  "title" : "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass2_io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1_kotlin.String-kotlin.Int"
                },
                "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1_kotlin.String-kotlin.Int": {
                  "type" : "object",
                  "properties" : {
                    "value1" : {
                      "type" : "string",
                      "title" : "kotlin.String"
                    },
                    "value2" : {
                      "type" : "integer",
                      "format" : "int32",
                      "title" : "kotlin.Int"
                    }
                  },
                  "required" : [ "value1", "value2" ],
                  "title" : "io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1_kotlin.String-kotlin.Int"
                }
              }
            }
            """.trimIndent()
        expectedSwaggerReferenceKotlinxSerialization = expectedSwaggerReference!!
            .replace("NestedClass1_String-Int", "NestedClass1")
            .replace("NestedClass1_kotlin.String-kotlin.Int", "NestedClass1")
            .replace("NestedClass2_NestedClass1_String-Int", "NestedClass2")
            .replace("NestedClass2_NestedClass1", "NestedClass2")
            .replace("NestedClass2_io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1_kotlin.String-kotlin.Int", "NestedClass2")
            .replace("NestedClass2_io.github.smiley4.schemakenerator.test.cases.TitleTestCases.NestedClass1", "NestedClass2")
        expectedJson = null
    }

    @Serializable
    class TestClass(
        val nested: NestedClass2<NestedClass1<String, Int>>
    )

    @Serializable
    class NestedClass1<T1, T2>(
        val value1: T1,
        val value2: T2
    )

    @Serializable
    class NestedClass2<T>(
        val someValue: T,
    )

}