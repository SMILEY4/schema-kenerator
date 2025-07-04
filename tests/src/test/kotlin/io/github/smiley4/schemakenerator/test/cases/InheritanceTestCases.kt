package io.github.smiley4.schemakenerator.test.cases

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.smiley4.schemakenerator.core.CoreSteps.addDiscriminatorProperty
import io.github.smiley4.schemakenerator.core.CoreSteps.addMissingSupertypeSubtypeRelations
import io.github.smiley4.schemakenerator.jackson.JacksonSteps.addJacksonTypeInfoDiscriminatorProperty
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.withTitle
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.reflection.data.SubType
import io.github.smiley4.schemakenerator.serialization.SerializationSteps.addJsonClassDiscriminatorProperty
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.withTitle
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.reflect.typeOf

object InheritanceTestCases {

    val supertypeWithSubtypes = case("inheritance", "supertype with subtypes") {
        type = typeOf<SealedClass>()
        // language=json
        expectedSwaggerInline = """
            {
              "schemas" : {
                "_root" : {
                  "anyOf": [
                    {
                      "required": [
                        "a",
                        "sealedValue"
                      ],
                      "type": "object",
                      "properties": {
                        "a": {
                          "type": "integer",
                          "format": "int32"
                        },
                        "sealedValue": {
                          "type": "string"
                        }
                      }
                    },
                    {
                      "required": [
                        "b",
                        "sealedValue"
                      ],
                      "type": "object",
                      "properties": {
                        "b": {
                          "type": "integer",
                          "format": "int32"
                        },
                        "sealedValue": {
                          "type": "string"
                        }
                      }
                    }
                  ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedSwaggerReference = """
            {
              "schemas" : {
                "_root" : {
                  "anyOf": [
                    {
                        "${'$'}ref": "#/components/schemas/SubClassA"
                    },
                    {
                        "${'$'}ref": "#/components/schemas/SubClassB"
                    }
                  ]
                },
                "SubClassA": {
                  "type": "object",
                  "required": [
                      "a",
                      "sealedValue"
                  ],
                  "properties": {
                      "a": {
                          "type": "integer",
                          "format": "int32"
                      },
                      "sealedValue": {
                          "type": "string"
                      }
                  }
                },
                "SubClassB": {
                   "type": "object",
                   "required": [
                       "b",
                       "sealedValue"
                   ],
                   "properties": {
                       "b": {
                           "type": "integer",
                           "format": "int32"
                       },
                       "sealedValue": {
                           "type": "string"
                       }
                   }
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJsonInline = """
            {
              "anyOf": [
                {
                  "required": [
                    "a",
                    "sealedValue"
                  ],
                  "type": "object",
                  "properties": {
                    "a": {
                      "type": "integer",
                      "minimum": -2147483648,
                      "maximum": 2147483647
                    },
                    "sealedValue": {
                      "type": "string"
                    }
                  }
                },
                {
                  "required": [
                    "b",
                    "sealedValue"
                  ],
                  "type": "object",
                  "properties": {
                    "b": {
                      "type": "integer",
                      "minimum": -2147483648,
                      "maximum": 2147483647
                    },
                    "sealedValue": {
                      "type": "string"
                    }
                  }
                }
              ]
            }
            """.trimIndent()
        // language=json
        expectedJsonReference = """
            {
              "anyOf": [
                {
                  "${'$'}ref": "#/${'$'}defs/SubClassA"
                },
                {
                  "${'$'}ref": "#/${'$'}defs/SubClassB"
                }
              ],
              "definitions": {
                "SubClassA": {
                  "type": "object",
                  "required": [
                    "a",
                    "sealedValue"
                  ],
                  "properties": {
                    "a": {
                      "type": "integer",
                      "minimum": -2147483648,
                      "maximum": 2147483647
                    },
                    "sealedValue": {
                        "type": "string"
                    }
                  }
                },
                "SubClassB": {
                  "type": "object",
                  "required": [
                   "b",
                   "sealedValue"
                  ],
                  "properties": {
                    "b": {
                      "type": "integer",
                      "minimum": -2147483648,
                      "maximum": 2147483647
                    },
                    "sealedValue": {
                      "type": "string"
                    }
                  }
                }
              }
            }
            """.trimIndent()
    }

    val subtypeWithSupertype = case("inheritance", "subtype with supertype") {
        type = typeOf<SubClassA>()
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "required": [
                      "a",
                      "sealedValue"
                  ],
                  "type": "object",
                  "properties": {
                      "a": {
                          "type": "integer",
                          "format": "int32"
                      },
                      "sealedValue": {
                          "type": "string"
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
              "required": ["a","sealedValue"],
              "properties": {
                  "a": {
                      "type": "integer",
                      "minimum": -2147483648,
                      "maximum": 2147483647
                  },
                  "sealedValue": {
                      "type": "string"
                  }
              }
            }
            """.trimIndent()
    }

    val basicDiscriminator = case("inheritance", "basic discriminator") {
        type = typeOf<SimpleTestClass>()
        postAnalyze = {
            this.addDiscriminatorProperty("_type")
        }
        // language=json
        expectedSwaggerInline = """
            {
              "schemas" : {
                "_root" : {
                  "anyOf": [
                    {
                      "type": "object",
                      "properties": {
                        "_type": {
                          "type": "string"
                        }
                      },
                      "required": [
                        "_type"
                      ]
                    },
                    {
                      "type": "object",
                      "properties": {
                        "_type": {
                          "type": "string"
                        }
                      },
                      "required": [
                        "_type"
                      ]
                    }
                  ],
                  "discriminator": {
                    "propertyName": "_type"
                  }
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedSwaggerReference = """
            {
              "schemas": {
                "_root": {
                  "anyOf": [
                    {
                      "${'$'}ref": "#/components/schemas/TestSubClass1"
                    },
                    {
                      "${'$'}ref": "#/components/schemas/TestSubClass2"
                    }
                  ],
                  "discriminator": {
                    "propertyName": "_type",
                    "mapping": {
                      "io.github.smiley4.schemakenerator.test.cases.InheritanceTestCases.TestSubClass1": "#/components/schemas/TestSubClass1",
                      "io.github.smiley4.schemakenerator.test.cases.InheritanceTestCases.TestSubClass2": "#/components/schemas/TestSubClass2"
                    }
                  }
                },
                "TestSubClass1": {
                  "type": "object",
                  "properties": {
                    "_type": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "_type"
                  ]
                },
                "TestSubClass2": {
                  "type": "object",
                  "properties": {
                    "_type": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "_type"
                  ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJsonInline = """
            {
              "anyOf": [
                {
                  "type": "object",
                  "required": [
                    "_type"
                  ],
                  "properties": {
                    "_type": {
                      "type": "string"
                    }
                  }
                },
                {
                  "type": "object",
                  "required": [
                    "_type"
                  ],
                  "properties": {
                    "_type": {
                      "type": "string"
                    }
                  }
                }
              ]
            }
            """.trimIndent()
        // language=json
        expectedJsonReference = """
            {
              "anyOf": [
                {
                  "${'$'}ref": "#/${'$'}defs/TestSubClass1"
                },
                {
                  "${'$'}ref": "#/${'$'}defs/TestSubClass2"
                }
              ],
              "definitions": {
                "TestSubClass1": {
                  "type": "object",
                  "required": [
                    "_type"
                  ],
                  "properties": {
                    "_type": {
                      "type": "string"
                    }
                  }
                },
                "TestSubClass2": {
                  "type": "object",
                  "required": [
                    "_type"
                  ],
                  "properties": {
                    "_type": {
                      "type": "string"
                    }
                  }
                }
              }
            }
            """.trimIndent()
    }

    val jacksonUseClassDiscriminator = case("inheritance", "jackson discriminator (use class)") {
        type = typeOf<JacksonUseClassTestClass>()
        withKotlinxSerialization = false // jackson annotations not supported by kotlinx-serialization
        postAnalyze = {
            this.addJacksonTypeInfoDiscriminatorProperty()
        }
        // language=json
        expectedSwaggerInline = null
        // language=json
        expectedSwaggerReference = """
            {
              "schemas": {
                "_root": {
                  "anyOf": [
                    {
                      "${'$'}ref": "#/components/schemas/JacksonUseClassTestSubClass1"
                    },
                    {
                      "${'$'}ref": "#/components/schemas/JacksonUseClassTestSubClass2"
                    }
                  ],
                  "discriminator": {
                    "propertyName": "_type",
                    "mapping": {
                      "io.github.smiley4.schemakenerator.test.cases.InheritanceTestCases.JacksonUseClassTestSubClass1": "#/components/schemas/JacksonUseClassTestSubClass1",
                      "io.github.smiley4.schemakenerator.test.cases.InheritanceTestCases.JacksonUseClassTestSubClass2": "#/components/schemas/JacksonUseClassTestSubClass2"
                    }
                  }
                },
                "JacksonUseClassTestSubClass1": {
                  "type": "object",
                  "properties": {
                    "_type": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "_type"
                  ]
                },
                "JacksonUseClassTestSubClass2": {
                  "type": "object",
                  "properties": {
                    "_type": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "_type"
                  ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJsonInline = null
        // language=json
        expectedJsonReference = """
            {
              "anyOf": [
                {
                  "${'$'}ref": "#/${'$'}defs/JacksonUseClassTestSubClass1"
                },
                {
                  "${'$'}ref": "#/${'$'}defs/JacksonUseClassTestSubClass2"
                }
              ],
              "definitions": {
                "JacksonUseClassTestSubClass1": {
                  "type": "object",
                  "required": [
                    "_type"
                  ],
                  "properties": {
                    "_type": {
                      "type": "string"
                    }
                  }
                },
                "JacksonUseClassTestSubClass2": {
                  "type": "object",
                  "required": [
                    "_type"
                  ],
                  "properties": {
                    "_type": {
                      "type": "string"
                    }
                  }
                }
              }
            }
            """.trimIndent()
    }

    val jacksonUseNameDiscriminator = case("inheritance", "jackson discriminator (use name)") {
        type = typeOf<JacksonUseNameTestClass>()
        withKotlinxSerialization = false // jackson annotations not supported by kotlinx-serialization
        postAnalyze = {
            this.addJacksonTypeInfoDiscriminatorProperty()
        }
        // language=json
        expectedSwaggerInline = null
        // language=json
        expectedSwaggerReference = """
            {
              "schemas": {
                "_root": {
                  "anyOf": [
                    {
                      "${'$'}ref": "#/components/schemas/JacksonUseNameTestSubClass1"
                    },
                    {
                      "${'$'}ref": "#/components/schemas/JacksonUseNameTestSubClass2"
                    }
                  ],
                  "discriminator": {
                    "propertyName": "_type",
                    "mapping": {
                      "test_1": "#/components/schemas/JacksonUseNameTestSubClass1",
                      "test_2": "#/components/schemas/JacksonUseNameTestSubClass2"
                    }
                  }
                },
                "JacksonUseNameTestSubClass1": {
                  "type": "object",
                  "properties": {
                    "_type": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "_type"
                  ]
                },
                "JacksonUseNameTestSubClass2": {
                  "type": "object",
                  "properties": {
                    "_type": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "_type"
                  ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJsonInline = null
        // language=json
        expectedJsonReference = """
            {
              "anyOf": [
                {
                  "${'$'}ref": "#/${'$'}defs/JacksonUseNameTestSubClass1"
                },
                {
                  "${'$'}ref": "#/${'$'}defs/JacksonUseNameTestSubClass2"
                }
              ],
              "definitions": {
                "JacksonUseNameTestSubClass1": {
                  "type": "object",
                  "required": [
                    "_type"
                  ],
                  "properties": {
                    "_type": {
                      "type": "string"
                    }
                  }
                },
                "JacksonUseNameTestSubClass2": {
                  "type": "object",
                  "required": [
                    "_type"
                  ],
                  "properties": {
                    "_type": {
                      "type": "string"
                    }
                  }
                }
              }
            }
            """.trimIndent()
    }

    val jacksonUseSimpleNameDiscriminator = case("inheritance", "jackson discriminator (use simple name)") {
        type = typeOf<JacksonUseSimpleNameTestClass>()
        withKotlinxSerialization = false // jackson annotations not supported by kotlinx-serialization
        postAnalyze = {
            this.addJacksonTypeInfoDiscriminatorProperty()
        }
        // language=json
        expectedSwaggerInline = null
        // language=json
        expectedSwaggerReference = """
            {
              "schemas": {
                "_root": {
                  "anyOf": [
                    {
                      "${'$'}ref": "#/components/schemas/JacksonUseSimpleNameTestSubClass1"
                    },
                    {
                      "${'$'}ref": "#/components/schemas/JacksonUseSimpleNameTestSubClass2"
                    }
                  ],
                  "discriminator": {
                    "propertyName": "_type",
                    "mapping": {
                      "JacksonUseSimpleNameTestSubClass1": "#/components/schemas/JacksonUseSimpleNameTestSubClass1",
                      "JacksonUseSimpleNameTestSubClass2": "#/components/schemas/JacksonUseSimpleNameTestSubClass2"
                    }
                  }
                },
                "JacksonUseSimpleNameTestSubClass1": {
                  "type": "object",
                  "properties": {
                    "_type": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "_type"
                  ]
                },
                "JacksonUseSimpleNameTestSubClass2": {
                  "type": "object",
                  "properties": {
                    "_type": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "_type"
                  ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJsonInline = null
        // language=json
        expectedJsonReference = """
            {
              "anyOf": [
                {
                  "${'$'}ref": "#/${'$'}defs/JacksonUseSimpleNameTestSubClass1"
                },
                {
                  "${'$'}ref": "#/${'$'}defs/JacksonUseSimpleNameTestSubClass2"
                }
              ],
              "definitions": {
                "JacksonUseSimpleNameTestSubClass1": {
                  "type": "object",
                  "required": [
                    "_type"
                  ],
                  "properties": {
                    "_type": {
                      "type": "string"
                    }
                  }
                },
                "JacksonUseSimpleNameTestSubClass2": {
                  "type": "object",
                  "required": [
                    "_type"
                  ],
                  "properties": {
                    "_type": {
                      "type": "string"
                    }
                  }
                }
              }
            }
            """.trimIndent()
    }

    val kotlinxSerializationDiscriminator = case("inheritance", "kotlinx discriminator") {
        // todo: kotlinx analysis
        type = typeOf<KotlinxTestClass>()
        postAnalyze = {
            this.addJsonClassDiscriminatorProperty()
        }
        expectedSwaggerInline = null
        expectedSwaggerReference = null
        expectedJsonInline = null
        expectedJsonReference = null
    }

    val collectSubtypesCore = case("inheritance", "collect sub types using core @SubType annotation") {
        type = typeOf<BaseClass1>()
        withKotlinxSerialization = false
        postAnalyze = {
            this.addMissingSupertypeSubtypeRelations()
        }
        postGenerateSwaggerSchema = {
            this.withTitle(io.github.smiley4.schemakenerator.swagger.data.TitleType.OPENAPI_SIMPLE)
        }
        postGenerateJsonSchema = {
            this.withTitle(TitleType.SIMPLE)
        }
        // language=json
        expectedSwaggerInline = """
            {
              "schemas": {
                "_root": {
                  "anyOf": [
                    {
                      "type": "object",
                      "properties": {},
                      "title": "SubClass1A"
                    },
                    {
                      "type": "object",
                      "properties": {},
                      "title": "SubClass1B"
                    },
                    {
                      "type": "object",
                      "properties": {
                        "nested": {
                          "anyOf": [
                            {
                              "type": "object",
                              "properties": {},
                              "title": "SubClass2A"
                            },
                            {
                              "type": "object",
                              "properties": {},
                              "title": "SubClass2B"
                            }
                          ],
                          "title": "BaseClass2"
                        }
                      },
                      "required": [
                        "nested"
                      ],
                      "title": "SubClass1C"
                    }
                  ],
                  "title": "BaseClass1"
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedSwaggerReference = """
            {
              "schemas" : {
                "_root" : {
                  "anyOf" : [ {
                    "${'$'}ref" : "#/components/schemas/SubClass1A"
                  }, {
                    "${'$'}ref" : "#/components/schemas/SubClass1B"
                  }, {
                    "${'$'}ref" : "#/components/schemas/SubClass1C"
                  } ],
                  "title" : "BaseClass1"
                },
                "SubClass1A" : {
                  "type" : "object",
                  "properties" : { },
                  "title" : "SubClass1A"
                },
                "SubClass1B" : {
                  "type" : "object",
                  "properties" : { },
                  "title" : "SubClass1B"
                },
                "SubClass1C" : {
                  "type" : "object",
                  "properties" : {
                    "nested" : {
                      "${'$'}ref" : "#/components/schemas/BaseClass2"
                    }
                  },
                  "required" : [ "nested" ],
                  "title" : "SubClass1C"
                },
                "BaseClass2" : {
                  "anyOf" : [ {
                    "${'$'}ref" : "#/components/schemas/SubClass2A"
                  }, {
                    "${'$'}ref" : "#/components/schemas/SubClass2B"
                  } ],
                  "title" : "BaseClass2"
                },
                "SubClass2A" : {
                  "type" : "object",
                  "properties" : { },
                  "title" : "SubClass2A"
                },
                "SubClass2B" : {
                  "type" : "object",
                  "properties" : { },
                  "title" : "SubClass2B"
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJsonInline = """
            {
               "anyOf": [
                  {
                     "type": "object",
                     "required": [],
                     "properties": {},
                     "title": "SubClass1A"
                  },
                  {
                     "type": "object",
                     "required": [],
                     "properties": {},
                     "title": "SubClass1B"
                  },
                  {
                     "type": "object",
                     "required": [
                        "nested"
                     ],
                     "properties": {
                        "nested": {
                           "anyOf": [
                              {
                                 "type": "object",
                                 "required": [],
                                 "properties": {},
                                 "title": "SubClass2A"
                              },
                              {
                                 "type": "object",
                                 "required": [],
                                 "properties": {},
                                 "title": "SubClass2B"
                              }
                           ],
                           "title": "BaseClass2"
                        }
                     },
                     "title": "SubClass1C"
                  }
               ],
               "title": "BaseClass1"
            }
            """.trimIndent()
        // language=json
        expectedJsonReference = """
            {
               "anyOf": [
                  {
                     "${'$'}ref": "#/${'$'}defs/SubClass1A"
                  },
                  {
                     "${'$'}ref": "#/${'$'}defs/SubClass1B"
                  },
                  {
                     "${'$'}ref": "#/${'$'}defs/SubClass1C"
                  }
               ],
               "title": "BaseClass1",
               "definitions": {
                  "SubClass1A": {
                     "type": "object",
                     "required": [],
                     "properties": {},
                     "title": "SubClass1A"
                  },
                  "SubClass1B": {
                     "type": "object",
                     "required": [],
                     "properties": {},
                     "title": "SubClass1B"
                  },
                  "SubClass1C": {
                     "type": "object",
                     "required": [
                        "nested"
                     ],
                     "properties": {
                        "nested": {
                           "${'$'}ref": "#/${'$'}defs/BaseClass2"
                        }
                     },
                     "title": "SubClass1C"
                  },
                  "BaseClass2": {
                     "anyOf": [
                        {
                           "${'$'}ref": "#/${'$'}defs/SubClass2A"
                        },
                        {
                           "${'$'}ref": "#/${'$'}defs/SubClass2B"
                        }
                     ],
                     "title": "BaseClass2"
                  },
                  "SubClass2A": {
                     "type": "object",
                     "required": [],
                     "properties": {},
                     "title": "SubClass2A"
                  },
                  "SubClass2B": {
                     "type": "object",
                     "required": [],
                     "properties": {},
                     "title": "SubClass2B"
                  }
               }
            }
            """.trimIndent()
    }

    val collectSubtypesJackson = case("inheritance", "collect sub types using jackson @JsonSubTypes annotation") {
        type = typeOf<JacksonBaseClass1>()
        withKotlinxSerialization = false
        postAnalyze = {
            this.addMissingSupertypeSubtypeRelations()
        }
        postGenerateSwaggerSchema = {
            this.withTitle(io.github.smiley4.schemakenerator.swagger.data.TitleType.OPENAPI_SIMPLE)
        }
        postGenerateJsonSchema = {
            this.withTitle(TitleType.SIMPLE)
        }
        // language=json
        expectedSwaggerInline = """
            {
              "schemas" : {
                "_root" : {
                  "anyOf" : [ {
                    "type" : "object",
                    "properties" : { },
                    "title" : "JacksonSubClass1A"
                  }, {
                    "type" : "object",
                    "properties" : { },
                    "title" : "JacksonSubClass1B"
                  }, {
                    "type" : "object",
                    "properties" : {
                      "nested" : {
                        "anyOf" : [ {
                          "type" : "object",
                          "properties" : { },
                          "title" : "JacksonSubClass2A"
                        }, {
                          "type" : "object",
                          "properties" : { },
                          "title" : "JacksonSubClass2B"
                        } ],
                        "title" : "JacksonBaseClass2"
                      }
                    },
                    "required" : [ "nested" ],
                    "title" : "JacksonSubClass1C"
                  } ],
                  "title" : "JacksonBaseClass1"
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedSwaggerReference = """
            {
              "schemas" : {
                "_root" : {
                  "anyOf" : [ {
                    "${'$'}ref" : "#/components/schemas/JacksonSubClass1A"
                  }, {
                    "${'$'}ref" : "#/components/schemas/JacksonSubClass1B"
                  }, {
                    "${'$'}ref" : "#/components/schemas/JacksonSubClass1C"
                  } ],
                  "title" : "JacksonBaseClass1"
                },
                "JacksonSubClass1A" : {
                  "type" : "object",
                  "properties" : { },
                  "title" : "JacksonSubClass1A"
                },
                "JacksonSubClass1B" : {
                  "type" : "object",
                  "properties" : { },
                  "title" : "JacksonSubClass1B"
                },
                "JacksonSubClass1C" : {
                  "type" : "object",
                  "properties" : {
                    "nested" : {
                      "${'$'}ref" : "#/components/schemas/JacksonBaseClass2"
                    }
                  },
                  "required" : [ "nested" ],
                  "title" : "JacksonSubClass1C"
                },
                "JacksonBaseClass2" : {
                  "anyOf" : [ {
                    "${'$'}ref" : "#/components/schemas/JacksonSubClass2A"
                  }, {
                    "${'$'}ref" : "#/components/schemas/JacksonSubClass2B"
                  } ],
                  "title" : "JacksonBaseClass2"
                },
                "JacksonSubClass2A" : {
                  "type" : "object",
                  "properties" : { },
                  "title" : "JacksonSubClass2A"
                },
                "JacksonSubClass2B" : {
                  "type" : "object",
                  "properties" : { },
                  "title" : "JacksonSubClass2B"
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJsonInline = """
            {
               "anyOf": [
                  {
                     "type": "object",
                     "required": [],
                     "properties": {},
                     "title": "JacksonSubClass1A"
                  },
                  {
                     "type": "object",
                     "required": [],
                     "properties": {},
                     "title": "JacksonSubClass1B"
                  },
                  {
                     "type": "object",
                     "required": [
                        "nested"
                     ],
                     "properties": {
                        "nested": {
                           "anyOf": [
                              {
                                 "type": "object",
                                 "required": [],
                                 "properties": {},
                                 "title": "JacksonSubClass2A"
                              },
                              {
                                 "type": "object",
                                 "required": [],
                                 "properties": {},
                                 "title": "JacksonSubClass2B"
                              }
                           ],
                           "title": "JacksonBaseClass2"
                        }
                     },
                     "title": "JacksonSubClass1C"
                  }
               ],
               "title": "JacksonBaseClass1"
            }
            """.trimIndent()
        // language=json
        expectedJsonReference = """
            {
               "anyOf": [
                  {
                     "${'$'}ref": "#/${'$'}defs/JacksonSubClass1A"
                  },
                  {
                     "${'$'}ref": "#/${'$'}defs/JacksonSubClass1B"
                  },
                  {
                     "${'$'}ref": "#/${'$'}defs/JacksonSubClass1C"
                  }
               ],
               "title": "JacksonBaseClass1",
               "definitions": {
                  "JacksonSubClass1A": {
                     "type": "object",
                     "required": [],
                     "properties": {},
                     "title": "JacksonSubClass1A"
                  },
                  "JacksonSubClass1B": {
                     "type": "object",
                     "required": [],
                     "properties": {},
                     "title": "JacksonSubClass1B"
                  },
                  "JacksonSubClass1C": {
                     "type": "object",
                     "required": [
                        "nested"
                     ],
                     "properties": {
                        "nested": {
                           "${'$'}ref": "#/${'$'}defs/JacksonBaseClass2"
                        }
                     },
                     "title": "JacksonSubClass1C"
                  },
                  "JacksonBaseClass2": {
                     "anyOf": [
                        {
                           "${'$'}ref": "#/${'$'}defs/JacksonSubClass2A"
                        },
                        {
                           "${'$'}ref": "#/${'$'}defs/JacksonSubClass2B"
                        }
                     ],
                     "title": "JacksonBaseClass2"
                  },
                  "JacksonSubClass2A": {
                     "type": "object",
                     "required": [],
                     "properties": {},
                     "title": "JacksonSubClass2A"
                  },
                  "JacksonSubClass2B": {
                     "type": "object",
                     "required": [],
                     "properties": {},
                     "title": "JacksonSubClass2B"
                  }
               }
            }
            """.trimIndent()
    }

    // ===== BASIC ==========================================

    @Serializable
    sealed class SealedClass {
        abstract val sealedValue: String
    }


    @Serializable
    class SubClassA(val a: Int, override val sealedValue: String) : SealedClass()


    @Serializable
    class SubClassB(val b: Int, override val sealedValue: String) : SealedClass()


    @Serializable
    private sealed class SimpleTestClass


    @Serializable
    private class TestSubClass1 : SimpleTestClass()


    @Serializable
    private class TestSubClass2 : SimpleTestClass()

    // ===== CORE SUBTYPE ANNOTATION ========================

    @SubType(SubClass1A::class)
    @SubType(SubClass1B::class)
    @SubType(SubClass1C::class)
    private open class BaseClass1
    private class SubClass1A : BaseClass1()
    private class SubClass1B : BaseClass1()
    private class SubClass1C(val nested: BaseClass2) : BaseClass1()


    @SubType(SubClass2A::class)
    @SubType(SubClass2B::class)
    private open class BaseClass2
    private class SubClass2A : BaseClass2()
    private class SubClass2B : BaseClass2()

    // ===== JACKSON OPEN CLASS =============================

    @JsonSubTypes(
        JsonSubTypes.Type(value = JacksonSubClass1A::class),
        JsonSubTypes.Type(value = JacksonSubClass1B::class),
        JsonSubTypes.Type(value = JacksonSubClass1C::class)
    )
    private open class JacksonBaseClass1
    private class JacksonSubClass1A : JacksonBaseClass1()
    private class JacksonSubClass1B : JacksonBaseClass1()
    private class JacksonSubClass1C(val nested: JacksonBaseClass2) : JacksonBaseClass1()


    @JsonSubTypes(
        JsonSubTypes.Type(value = JacksonSubClass2A::class),
        JsonSubTypes.Type(value = JacksonSubClass2B::class),
    )
    private open class JacksonBaseClass2
    private class JacksonSubClass2A : JacksonBaseClass2()
    private class JacksonSubClass2B : JacksonBaseClass2()

    // ===== JACKSON "CLASS" ================================

    @JsonTypeInfo(
        property = "_type",
        include = JsonTypeInfo.As.PROPERTY,
        use = JsonTypeInfo.Id.CLASS,
    )
    private sealed class JacksonUseClassTestClass
    private class JacksonUseClassTestSubClass1 : JacksonUseClassTestClass()
    private class JacksonUseClassTestSubClass2 : JacksonUseClassTestClass()

    // ===== JACKSON "NAME" =================================

    @JsonTypeInfo(
        property = "_type",
        include = JsonTypeInfo.As.PROPERTY,
        use = JsonTypeInfo.Id.NAME,
    )
    @JsonSubTypes(
        JsonSubTypes.Type(value = JacksonUseNameTestSubClass1::class, name = "test_1"),
        JsonSubTypes.Type(value = JacksonUseNameTestSubClass2::class, name = "test_2"),
    )
    private sealed class JacksonUseNameTestClass
    private class JacksonUseNameTestSubClass1 : JacksonUseNameTestClass()
    private class JacksonUseNameTestSubClass2 : JacksonUseNameTestClass()

    // ===== JACKSON "SIMPLE NAME" ==========================

    @JsonTypeInfo(
        property = "_type",
        include = JsonTypeInfo.As.PROPERTY,
        use = JsonTypeInfo.Id.SIMPLE_NAME,
    )
    private sealed class JacksonUseSimpleNameTestClass
    private class JacksonUseSimpleNameTestSubClass1 : JacksonUseSimpleNameTestClass()
    private class JacksonUseSimpleNameTestSubClass2 : JacksonUseSimpleNameTestClass()

    // ===== KOTLINX SERIALIZATION ==========================

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    @JsonClassDiscriminator("_myType")
    private sealed class KotlinxTestClass


    @Serializable
    private class KotlinxTestSubClass1 : KotlinxTestClass()


    @Serializable
    @SerialName("test_2")
    private class KotlinxTestSubClass2 : KotlinxTestClass()

}
