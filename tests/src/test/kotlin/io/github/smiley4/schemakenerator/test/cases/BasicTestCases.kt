package io.github.smiley4.schemakenerator.test.cases

import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

object BasicTestCases {

    val any = case("basics", "Any") {
        type = typeOf<Any>()
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type": "object"
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
              "type": "object"
            }
            """.trimIndent()
    }

    val uByte = case("basics", "UByte") {
        type = typeOf<UByte>()
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type": "integer",
                  "maximum": 255,
                  "minimum": 0
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
              "type": "integer",
              "maximum": 255,
              "minimum": 0
            }
            """.trimIndent()
    }

    val int = case("basics", "Int") {
        type = typeOf<Int>()
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type": "integer",
                  "format": "int32"
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
              "type": "integer",
              "minimum": -2147483648,
              "maximum": 2147483647
            }
            """.trimIndent()
    }

    val float = case("basics", "Float") {
        type = typeOf<Float>()
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type": "number",
                  "format": "float"
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
              "type": "number",
              "minimum": 1.4E-45,
              "maximum": 3.4028235E38
            }
            """.trimIndent()
    }

    val boolean = case("basics", "Boolean") {
        type = typeOf<Boolean>()
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type": "boolean"
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
              "type": "boolean"
            }
            """.trimIndent()
    }

    val string = case("basics", "String") {
        type = typeOf<String>()
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type": "string"
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
              "type": "string"
            }
            """.trimIndent()
    }

    val enum = case("basics", "enum") {
        type = typeOf<TestEnum>()
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                "type": "string",
                  "enum": [ "ONE", "TWO", "THREE" ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
              "enum": [ "ONE", "TWO", "THREE" ]
            }
            """.trimIndent()
    }

    val listOfStrings = case("basics", "List<String>") {
        type = typeOf<List<String>>()
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type": "array",
                  "items": {
                      "type": "string"
                  }
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
              "type": "array",
              "items": {
                  "type": "string"
              }
            }
            """.trimIndent()
    }

    val mapStringToInt = case("basics", "Map<String, Int>") {
        type = typeOf<Map<String, Int>>()
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type": "object",
                  "additionalProperties": {
                    "type": "integer",
                    "format": "int32"
                  }
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
              "type": "object",
              "additionalProperties": {
                "type": "integer",
                "minimum": -2147483648,
                "maximum": 2147483647
              }
            }
            """.trimIndent()
    }

    val simpleFields = case("basics", "simple fields") {
        type = typeOf<ClassWithSimpleFields>()
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type": "object",
                  "required": [
                    "someBoolList",
                    "someString"
                  ],
                  "properties": {
                    "someBoolList": {
                      "type": "array",
                      "items": {
                        "type": "boolean"
                      }
                    },
                    "someNullableInt": {
                      "type": ["integer", "null"],
                      "format": "int32"
                    },
                    "someString": {
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
              "required": [
                "someBoolList",
                "someString"
              ],
              "properties": {
                "someBoolList": {
                  "type": "array",
                  "items": {
                    "type": "boolean"
                  }
                },
                "someNullableInt": {
                  "type": ["integer", "null"],
                  "minimum": -2147483648,
                  "maximum": 2147483647
                },
                "someString": {
                  "type": "string"
                }
              }
            }
            """.trimIndent()
    }

    val optionalParametersAsRequired = case("basics", "optional parameters (as required)") {
        type = typeOf<ClassWithOptionalParameters>()
        swaggerConfig = {
            optionals = SwaggerSteps.RequiredHandling.REQUIRED
        }
        jsonConfig = {
            optionalHandling = JsonSchemaSteps.OptionalHandling.REQUIRED
        }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "required": [
                    "ctorOptional",
                    "ctorRequired"
                  ],
                  "type": "object",
                  "properties": {
                    "ctorOptional": {
                      "type": "string"
                    },
                    "ctorOptionalNullable": {
                      "type": ["string", "null"]
                    },
                    "ctorRequired": {
                      "type": "string"
                    },
                    "ctorRequiredNullable": {
                      "type": ["string", "null"]
                    }
                  }
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
              "required": [
                "ctorOptional",
                "ctorRequired"
              ],
              "type": "object",
              "properties": {
                "ctorOptional": {
                  "type": "string"
                },
                "ctorOptionalNullable": {
                  "type": ["string", "null"]
                },
                "ctorRequired": {
                  "type": "string"
                },
                "ctorRequiredNullable": {
                  "type": ["string", "null"]
                }
              }
            }
            """.trimIndent()
    }

    val optionalParametersAsNonRequired = case("basics", "optional parameters (as non-required)") {
        type = typeOf<ClassWithOptionalParameters>()
        swaggerConfig = {
            optionals = SwaggerSteps.RequiredHandling.NON_REQUIRED
        }
        jsonConfig = {
            optionalHandling = JsonSchemaSteps.OptionalHandling.NON_REQUIRED
        }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "required": [
                    "ctorRequired"
                  ],
                  "type": "object",
                  "properties": {
                    "ctorOptional": {
                      "type": "string"
                    },
                    "ctorOptionalNullable": {
                      "type": ["string", "null"]
                    },
                    "ctorRequired": {
                      "type": "string"
                    },
                    "ctorRequiredNullable": {
                      "type": ["string", "null"]
                    }
                  }
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
              "required": [
                "ctorRequired"
              ],
              "type": "object",
              "properties": {
                "ctorOptional": {
                  "type": "string"
                },
                "ctorOptionalNullable": {
                  "type": ["string", "null"]
                },
                "ctorRequired": {
                  "type": "string"
                },
                "ctorRequiredNullable": {
                  "type": ["string", "null"]
                }
              }
            }
            """.trimIndent()
    }

    val collections = case("basics", "collections") {
        type = typeOf<ClassWithCollections>()
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "required": [
                    "someArray",
                    "someList",
                    "someMap",
                    "someSet"
                  ],
                  "type": "object",
                  "properties": {
                    "someList": {
                      "type": "array",
                      "items": {
                        "type": "string"
                      }
                    },
                    "someSet": {
                      "type": "array",
                      "items": {
                        "type": "string"
                      },
                      "uniqueItems": true
                    },
                    "someMap": {
                      "type": "object",
                      "additionalProperties": {
                        "type": "integer",
                        "format": "int32"
                      }
                    },
                    "someArray": {
                      "type": "array",
                      "items": {
                        "type": "integer",
                        "format": "int32"
                      }
                    }
                  }
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
              "required": [
                "someArray",
                "someList",
                "someMap",
                "someSet"
              ],
              "type": "object",
              "properties": {
                "someList": {
                  "type": "array",
                  "items": {
                    "type": "string"
                  }
                },
                "someSet": {
                  "type": "array",
                  "items": {
                    "type": "string"
                  },
                  "uniqueItems": true
                },
                "someMap": {
                  "type": "object",
                  "additionalProperties": {
                    "type": "integer",
                    "minimum": -2147483648,
                    "maximum": 2147483647
                  }
                },
                "someArray": {
                  "type": "array",
                  "items": {
                    "type": "integer",
                    "minimum": -2147483648,
                    "maximum": 2147483647
                  }
                }
              }
            }
            """.trimIndent()
    }

    val valueClass = case("basics", "value class") {
        type = typeOf<ClassWithValueClass>()
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "required": [
                    "myValue",
                    "someText"
                  ],
                  "type": "object",
                  "properties": {
                    "myValue": {
                      "type": "integer",
                      "format": "int32"
                    },
                    "someText": {
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
              "required": [
                "myValue",
                "someText"
              ],
              "type": "object",
              "properties": {
                "myValue": {
                  "type": "integer",
                  "minimum": -2147483648,
                  "maximum": 2147483647
                },
                "someText": {
                  "type": "string"
                }
              }
            }
            """.trimIndent()
    }

    val nullabilityOfParametersOfSameTypeFirst = case("basics", "nullability of parameters of same type (first is nullable)") {
        type = typeOf<NullableClasses.NullableFirst>()
        swaggerConfig = {
            nullables = SwaggerSteps.RequiredHandling.REQUIRED
        }
        jsonConfig = {
            // todo
        }
        // language=json
        expectedSwaggerInline = """
            {
              "schemas" : {
                "_root" : {
                  "type": "object",
                  "properties": {
                    "t1": {
                      "type": [
                        "null",
                        "object"
                      ],
                      "properties": {
                        "count": {
                          "type": "integer",
                          "format": "int32"
                        }
                      },
                      "required": [
                        "count"
                      ]
                    },
                    "t2": {
                      "type": "object",
                      "properties": {
                        "count": {
                          "type": "integer",
                          "format": "int32"
                        }
                      },
                      "required": [
                        "count"
                      ]
                    }
                  },
                  "required": [
                    "t1",
                    "t2"
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
                  "type": "object",
                  "properties": {
                    "t1": {
                      "oneOf": [
                        {
                          "type": "null"
                        },
                        {
                          "${'$'}ref": "#/components/schemas/Test"
                        }
                      ]
                    },
                    "t2": {
                      "${'$'}ref": "#/components/schemas/Test"
                    }
                  },
                  "required": [
                    "t1",
                    "t2"
                  ]
                },
                "Test": {
                  "type": "object",
                  "properties": {
                    "count": {
                      "type": "integer",
                      "format": "int32"
                    }
                  },
                  "required": [
                    "count"
                  ]
                }
              }
            }
        """.trimIndent()
        // language=json
        expectedJsonInline = """
            {
              "type": "object",
              "properties": {
                "t1": {
                  "type": [
                    "null",
                    "object"
                  ],
                  "properties": {
                    "count": {
                      "type": "integer",
                      "minimum": -2147483648,
                      "maximum": 2147483647
                    }
                  },
                  "required": [
                    "count"
                  ]
                },
                "t2": {
                  "type": "object",
                  "properties": {
                    "count": {
                      "type": "integer",
                      "minimum": -2147483648,
                      "maximum": 2147483647
                    }
                  },
                  "required": [
                    "count"
                  ]
                }
              },
              "required": [
                "t1",
                "t2"
              ]
            }
            """.trimIndent()
        // language=json
        expectedJsonReference = """
            {
              "type": "object",
              "properties": {
                "t1": {
                  "oneOf": [
                    {
                      "type": "null"
                    },
                    {
                      "${'$'}ref": "#/definitions/Test"
                    }
                  ]
                },
                "t2": {
                  "${'$'}ref": "#/definitions/Test"
                }
              },
              "required": [
                "t1",
                "t2"
              ],
              "definitions": {
                "Test": {
                  "type": "object",
                  "properties": {
                    "count": {
                      "type": "integer",
                      "minimum": -2147483648,
                      "maximum": 2147483647
                    }
                  },
                  "required": [
                    "count"
                  ]
                }
              }
            }
            """.trimIndent()
    }

    val nullabilityOfParametersOfSameTypeSecond = case("basics", "nullability of parameters of same type (second is nullable)") {
        type = typeOf<NullableClasses.NullableSecond>()
        swaggerConfig = {
            nullables = SwaggerSteps.RequiredHandling.REQUIRED
        }
        jsonConfig = {
            // todo
        }
        // language=json
        expectedSwaggerInline = """
            {
              "schemas" : {
                "_root" : {
                  "type": "object",
                  "properties": {
                    "t1": {
                      "type": "object",
                      "properties": {
                        "count": {
                          "type": "integer",
                          "format": "int32"
                        }
                      },
                      "required": [
                        "count"
                      ]
                    },
                    "t2": {
                      "type": [
                        "null",
                        "object"
                      ],
                      "properties": {
                        "count": {
                          "type": "integer",
                          "format": "int32"
                        }
                      },
                      "required": [
                        "count"
                      ]
                    }
                  },
                  "required": [
                    "t1",
                    "t2"
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
                  "type": "object",
                  "properties": {
                    "t1": {
                      "${'$'}ref": "#/components/schemas/Test"
                    },
                    "t2": {
                      "oneOf": [
                        {
                          "type": "null"
                        },
                        {
                          "${'$'}ref": "#/components/schemas/Test"
                        }
                      ]
                    }
                  },
                  "required": [
                    "t1",
                    "t2"
                  ]
                },
                "Test": {
                  "type": "object",
                  "properties": {
                    "count": {
                      "type": "integer",
                      "format": "int32"
                    }
                  },
                  "required": [
                    "count"
                  ]
                }
              }
            }
        """.trimIndent()
        // language=json
        expectedJsonInline = """
            {
              "type": "object",
              "properties": {
                "t1": {
                  "type": "object",
                  "properties": {
                    "count": {
                      "type": "integer",
                      "minimum": -2147483648,
                      "maximum": 2147483647
                    }
                  },
                  "required": [
                    "count"
                  ]
                },
                "t2": {
                  "type": [
                    "null",
                    "object"
                  ],
                  "properties": {
                    "count": {
                      "type": "integer",
                      "minimum": -2147483648,
                      "maximum": 2147483647
                    }
                  },
                  "required": [
                    "count"
                  ]
                }
              },
              "required": [
                "t1",
                "t2"
              ]
            }
        """.trimIndent()
        // language=json
        expectedJsonReference = """
            {
              "type": "object",
              "properties": {
                "t1": {
                  "${'$'}ref": "#/definitions/Test"
                },
                "t2": {
                  "oneOf": [
                    {
                      "type": "null"
                    },
                    {
                      "${'$'}ref": "#/definitions/Test"
                    }
                  ]
                }
              },
              "required": [
                "t1",
                "t2"
              ],
            "definitions": {
              "Test": {
                "type": "object",
                "properties": {
                  "count": {
                    "type": "integer",
                    "minimum": -2147483648,
                    "maximum": 2147483647
                  }
                },
                "required": [
                  "count"
                ]
              }
            }
        """.trimIndent()
    }

    val genericField = case("basics", "generic field") {
        type = typeOf<ClassWithGenericField<String>>()
        // language=json
        expectedSwagger = """
             {
               "schemas": {
                 "_root": {
                   "type": "object",
                   "properties": {
                     "value": {
                       "type": "string"
                     }
                   },
                   "required": [
                     "value"
                   ]
                 }
               }
             }
        """.trimIndent()
        // language=json
        expectedJson = """
             {
               "type": "object",
               "properties": {
                 "value": {
                   "type": "string"
                 }
               },
               "required": [
                 "value"
               ]
             }
        """.trimIndent()
    }

    val genericNullableField = case("basics", "generic nullable field") {
        type = typeOf<ClassWithGenericField<String?>>()
        // language=json
        expectedSwagger = """
             {
               "schemas": {
                 "_root": {
                   "type": "object",
                   "properties": {
                     "value": {
                       "type": ["string", "null"]
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
               "properties": {
                 "value": {
                   "type": ["string", "null"]
                 }
               },
               "required": []
             }
        """.trimIndent()
    }

    val genericComplexField = case("basics", "generic complex field") {
        type = typeOf<ClassWithGenericField<NestedClass>>()
        // language=json
        expectedSwaggerInline = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "value": {
                      "type": "object",
                      "properties": {
                        "text": {
                          "type": "string"
                        }
                      },
                      "required": [
                        "text"
                      ]
                    }
                  },
                  "required": [
                    "value"
                  ]
                }
              }
            }
        """.trimIndent()
        // language=json
        expectedSwaggerReference = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "value": {
                      "${'$'}ref": "#/components/schemas/NestedClass"
                    }
                  },
                  "required": [
                    "value"
                  ]
                },
                "NestedClass": {
                  "type": "object",
                  "properties": {
                    "text": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "text"
                  ]
                }
              }
            }
        """.trimIndent()
        // language=json
        expectedJsonInline = """
            {
              "type": "object",
              "properties": {
                "value": {
                  "type": "object",
                  "properties": {
                    "text": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "text"
                  ]
                }
              },
              "required": [
                "value"
              ]
            }
        """.trimIndent()
        // language=json
        expectedJsonReference = """
            {
              "type": "object",
              "properties": {
                "value": {
                  "${'$'}ref": "#/definitions/NestedClass"
                }
              },
              "required": [
                "value"
              ],
              "definitions": {
                "NestedClass": {
                  "type": "object",
                  "properties": {
                    "text": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "text"
                  ]
                }
              }
            }
        """.trimIndent()
    }

    val genericWildcardField = case("basics", "generic wildcard field") {
        type = typeOf<ClassWithGenericField<*>>()
        // language=json
        expectedSwagger = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "value": {
                      "type": "object"
                    }
                  },
                  "required": [
                    "value"
                  ]
                }
              }
            }
        """.trimIndent()
        // language=json
        expectedSwaggerKotlinxSerialization = """
            {
              "schemas" : {
                "_root" : {
                  "type": "object"
                }
              }
            }
        """.trimIndent()
        // language=json
        expectedJson = """
            {
              "type": "object",
              "properties": {
                "value": {
                  "type": "object"
                }
              },
              "required": [
                "value"
              ]
            }
        """.trimIndent()
        // language=json
        expectedJsonKotlinxSerialization = """
            {
              "type": "object"
            }
        """.trimIndent()
    }

    val genericDeepField = case("basics", "generic deep field") {
        type = typeOf<ClassWithDeepGeneric<String>>()
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type": "object",
                  "properties": {
                    "value": {
                      "type": "array",
                      "items": {
                        "type": "string"
                      }
                    }
                  },
                  "required": [
                    "value"
                  ]
                }
              }
            }
        """.trimIndent()
        // language=json
        expectedJson = """
            {
              "type": "object",
              "properties": {
                "value": {
                  "type": "array",
                  "items": {
                    "type": "string"
                  }
                }
              },
              "required": [
                "value"
              ]
            }
        """.trimIndent()
    }

    val differentGenericsForSameWrapper = case("basics", "different generics for same wrapper") {
        type = typeOf<ClassWithDifferentGenerics>()
        // language=json
        expectedSwaggerInline = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "valueInt": {
                      "type": "object",
                      "properties": {
                        "value": {
                          "type": "integer",
                          "format": "int32"
                        }
                      },
                      "required": [
                        "value"
                      ]
                    },
                    "valueString": {
                      "type": "object",
                      "properties": {
                        "value": {
                          "type": "string"
                        }
                      },
                      "required": [
                        "value"
                      ]
                    }
                  },
                  "required": [
                    "valueInt",
                    "valueString"
                  ]
                }
              }
            }
        """.trimIndent()
        // language=json
        expectedSwaggerReference = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "valueInt": {
                      "${'$'}ref": "#/components/schemas/ClassWithGenericField_Int"
                    },
                    "valueString": {
                      "${'$'}ref": "#/components/schemas/ClassWithGenericField_String"
                    }
                  },
                  "required": [
                    "valueInt",
                    "valueString"
                  ]
                },
                "ClassWithGenericField_Int": {
                  "type": "object",
                  "properties": {
                    "value": {
                      "type": "integer",
                      "format": "int32"
                    }
                  },
                  "required": [
                    "value"
                  ]
                },
                "ClassWithGenericField_String": {
                  "type": "object",
                  "properties": {
                    "value": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "value"
                  ]
                }
              }
            }
        """.trimIndent()
        expectedSwaggerReferenceKotlinxSerialization = expectedSwaggerReference
            ?.replace("ClassWithGenericField_Int", "ClassWithGenericField")
            ?.replace("ClassWithGenericField_String", "ClassWithGenericField2")
        // language=json
        expectedJsonInline = """
            {
              "type": "object",
              "required": [
                "valueInt",
                "valueString"
              ],
              "properties": {
                "valueInt": {
                  "type": "object",
                  "required": [
                    "value"
                  ],
                  "properties": {
                    "value": {
                      "type": "integer",
                      "minimum": -2147483648,
                      "maximum": 2147483647
                    }
                  }
                },
                "valueString": {
                  "type": "object",
                  "required": [
                    "value"
                  ],
                  "properties": {
                    "value": {
                      "type": "string"
                    }
                  }
                }
              }
            }
        """.trimIndent()
        // language=json
        expectedJsonReference = """
            {
              "type": "object",
              "required": [
                "valueInt",
                "valueString"
              ],
              "properties": {
                "valueInt": {
                  "${'$'}ref": "#/definitions/ClassWithGenericField<Int>"
                },
                "valueString": {
                  "${'$'}ref": "#/definitions/ClassWithGenericField<String>"
                }
              },
              "definitions": {
                "ClassWithGenericField<Int>": {
                  "type": "object",
                  "required": [
                    "value"
                  ],
                  "properties": {
                    "value": {
                      "type": "integer",
                      "minimum": -2147483648,
                      "maximum": 2147483647
                    }
                  }
                },
                "ClassWithGenericField<String>": {
                  "type": "object",
                  "required": [
                    "value"
                  ],
                  "properties": {
                    "value": {
                      "type": "string"
                    }
                  }
                }
              }
            }
        """.trimIndent()
        expectedJsonReferenceKotlinxSerialization = expectedJsonReference
            ?.replace("ClassWithGenericField<Int>", "ClassWithGenericField")
            ?.replace("ClassWithGenericField<String>", "ClassWithGenericField2")
    }

    val nestedClass = case("basics", "nested class") {
        type = typeOf<ClassWithNestedClass>()
        // language=json
        expectedSwaggerInline = """
            {
              "schemas" : {
                "_root" : {
                  "required": [
                    "nested"
                  ],
                  "type": "object",
                  "properties": {
                    "nested": {
                      "type": "object",
                      "required": [
                        "text"
                      ],
                      "properties": {
                        "text": {
                          "type": "string"
                        }
                      }
                    }
                  }
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedSwaggerReference = """
            {
              "schemas" : {
                "_root" : {
                  "required": [
                    "nested"
                  ],
                  "type": "object",
                  "properties": {
                    "nested": {
                      "${'$'}ref": "#/components/schemas/NestedClass"
                    }
                  }
                },
                "NestedClass": {
                  "required": [
                    "text"
                  ],
                  "type": "object",
                  "properties": {
                    "text": {
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
              "required": [
                "nested"
              ],
              "type": "object",
              "properties": {
                "nested": {
                  "type": "object",
                  "required": [
                    "text"
                  ],
                  "properties": {
                    "text": {
                      "type": "string"
                    }
                  }
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJsonReference = """
            {
              "required": [
                "nested"
              ],
              "type": "object",
              "properties": {
                "nested": {
                  "${'$'}ref": "#/definitions/NestedClass"
                }
              },
              "definitions": {
                 "NestedClass": {
                   "required": [
                     "text"
                   ],
                   "type": "object",
                   "properties": {
                     "text": {
                       "type": "string"
                     }
                  }
                }
              }
            }
            """.trimIndent()
    }

    val nullableSelfReference = case("basics", "nullable self reference") {
        type = typeOf<ClassDirectSelfReferencing>()
        // language=json
        expectedSwaggerInline = """
            {
              "schemas" : {
                "_root" : {
                  "type": "object",
                  "properties": {
                    "self": {
                      "${'$'}ref": "#"
                    }
                  }
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedSwaggerReference = """
            {
              "schemas" : {
                "_root" : {
                  "type": "object",
                  "properties": {
                    "self": {
                      "oneOf": [
                        {
                          "type": "null"
                        },
                        {
                          "${'$'}ref": "#/components/schemas/ClassDirectSelfReferencing"
                        }
                      ]
                    }
                  }
                },
                "ClassDirectSelfReferencing": {
                  "type": "object",
                  "properties": {
                    "self": {
                      "oneOf": [
                        {
                          "type": "null"
                        },
                        {
                          "${'$'}ref": "#/components/schemas/ClassDirectSelfReferencing"
                        }
                      ]
                    }
                  }
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJsonInline = """
            {
              "type": "object",
              "properties": {
                "self": {
                  "${'$'}ref": "#"
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJsonReference = """
            {
              "type": "object",
              "properties": {
                "self": {
                  "oneOf": [
                    {
                      "type": "null"
                    },
                    {
                      "${'$'}ref": "#/definitions/ClassDirectSelfReferencing"
                    }
                  ]
                }
              },
              "definitions": {
                "ClassDirectSelfReferencing": {
                  "type": "object",
                  "properties": {
                    "self": {
                      "oneOf": [
                        {
                          "type": "null"
                        },
                        {
                          "${'$'}ref": "#/definitions/ClassDirectSelfReferencing"
                        }
                      ]
                    }
                  }
                }
              }
            }
            """.trimIndent()
    }


    @Serializable
    enum class TestEnum {
        ONE, TWO, THREE
    }


    @Serializable
    class ClassWithSimpleFields(
        val someString: String,
        val someNullableInt: Int?,
        val someBoolList: List<Boolean>,
    )


    @Serializable
    class ClassWithCollections(
        val someList: List<String>,
        val someSet: Set<String>,
        val someMap: Map<String, Int>,
        val someArray: IntArray,
    )


    @Serializable
    class ClassWithNestedClass(
        val nested: NestedClass
    )


    @Serializable
    class NestedClass(val text: String)


    @Serializable
    class ClassDirectSelfReferencing(
        val self: ClassDirectSelfReferencing?
    )


    @Serializable
    class ClassWithDeepGeneric<T>(
        val value: List<T>
    )


    @Serializable
    class ClassWithGenericField<T>(
        val value: T
    )


    @Serializable
    class ClassWithDifferentGenerics(
        val valueInt: ClassWithGenericField<Int>,
        val valueString: ClassWithGenericField<String>
    )


    @Serializable
    class ClassWithOptionalParameters(
        val ctorRequired: String,
        val ctorOptional: String = "test",
        val ctorRequiredNullable: String?,
        val ctorOptionalNullable: String? = null
    )


    @JvmInline
    @Serializable
    value class ValueClass(val inlinedValue: Int)


    @Serializable
    data class ClassWithValueClass(val myValue: ValueClass, val someText: String)

    interface NullableClasses {
        @Serializable
        class NullableFirst(val t1: Test?, val t2: Test)


        @Serializable
        class NullableSecond(val t1: Test, val t2: Test?)


        @Serializable
        class Test(val count: Int)
    }

}