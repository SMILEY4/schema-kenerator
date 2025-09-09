@file:OptIn(ExperimentalSerializationApi::class)

package io.github.smiley4.schemakenerator.test.cases

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.smiley4.schemakenerator.core.CoreSteps.renameMembers
import io.github.smiley4.schemakenerator.core.annotations.Description
import io.github.smiley4.schemakenerator.core.annotations.Format
import io.github.smiley4.schemakenerator.core.annotations.Required
import io.github.smiley4.schemakenerator.core.annotations.Type
import io.github.smiley4.schemakenerator.jackson.JacksonSteps.handleJacksonAnnotations
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.customizeProperties
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.handleCoreAnnotations
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.mergePropertyAttributesIntoType
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.withTitle
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.serialization.SerializationSteps.renameMembers
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.customizeProperties
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.handleCoreAnnotations
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.handleSchemaAnnotations
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.mergePropertyAttributesIntoType
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.withTitle
import io.github.smiley4.schemakenerator.validation.swagger.ValidationSwaggerSteps.handleJavaxValidationAnnotations
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.time.Instant
import java.util.UUID
import javax.validation.constraints.Size
import kotlin.reflect.typeOf
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType as JsonTitleType
import io.github.smiley4.schemakenerator.swagger.data.TitleType as SwaggerTitleType

object MiscTestCases {

    val requiredAnnotationAllPropsNullableOrOptional = case(
        "misc",
        "required annotation all props nullable or optional - https://github.com/SMILEY4/schema-kenerator/issues/19"
    ) {
        type = typeOf<TestClassIssue19>()
        postGenerateJsonSchema = { this.handleCoreAnnotations() }
        postGenerateSwaggerSchema = { this.handleCoreAnnotations() }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "prop1" : {
                      "type" : [ "null", "string" ]
                    },
                    "prop2" : {
                      "type" : [ "null", "string" ]
                    }
                  },
                  "required" : [ "prop1" ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
               "type": "object",
               "required": [
                  "prop1"
               ],
               "properties": {
                  "prop1": {
                     "type" : [ "null", "string" ]
                  },
                  "prop2": {
                     "type" : [ "null", "string" ]
                  }
               }
            }
            """.trimIndent()
    }


    @Serializable
    data class TestClassIssue19(
        @Required
        val prop1: String?,
        val prop2: String? = null
    )

    val includeAnnotationsFromConstructorParameters = case(
        "misc",
        "include annotations from constructor parameters - https://github.com/SMILEY4/schema-kenerator/issues/20"
    ) {
        type = typeOf<TestClassIssue20>()
        withKotlinxSerialization = false
        postAnalyze = { this.handleJacksonAnnotations() }
        postGenerateSwaggerSchema = { this.handleJavaxValidationAnnotations() }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "passwordRenamed" : {
                      "type" : "string",
                      "maxLength" : 200
                    },
                    "usernameRenamed" : {
                      "type" : "string",
                      "maxLength" : 100
                    }
                  },
                  "required" : [ "passwordRenamed", "usernameRenamed" ]
                }
              }
            }
            """.trimIndent()
    }

    data class TestClassIssue20(
        @field:Size(max = 100)
        @JsonProperty("usernameRenamed", required = true)
        val username: String?,

        @field:Size(max = 200)
        @JsonProperty("passwordRenamed", required = true)
        val password: String?
    )


    val renamePropertiesAddPrefix = case(
        "misc",
        "rename properties (add prefix) - https://github.com/SMILEY4/schema-kenerator/issues/18"
    ) {
        type = typeOf<TestClassIssue18>()
        postAnalyze = { this.renameMembers { name -> "prefix_$name" } }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "prefix_nameOfPerson" : {
                      "type" : "string"
                    },
                    "prefix_numberOfYears" : {
                      "type" : "integer",
                      "format" : "int32"
                    }
                  },
                  "required" : [ "prefix_nameOfPerson", "prefix_numberOfYears" ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
               "type": "object",
               "required": [
                  "prefix_nameOfPerson",
                  "prefix_numberOfYears"
               ],
               "properties": {
                  "prefix_nameOfPerson": {
                     "type": "string"
                  },
                  "prefix_numberOfYears": {
                     "type": "integer",
                     "minimum": -2147483648,
                     "maximum": 2147483647
                  }
               }
            }
            """.trimIndent()
    }

    val renamePropertiesSnakeCase = case(
        "misc",
        "rename properties (snake case) - https://github.com/SMILEY4/schema-kenerator/issues/18"
    ) {
        type = typeOf<TestClassIssue18>()
        postAnalyze = { this.renameMembers(JsonNamingStrategy.SnakeCase) }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "name_of_person" : {
                      "type" : "string"
                    },
                    "number_of_years" : {
                      "type" : "integer",
                      "format" : "int32"
                    }
                  },
                  "required" : [ "name_of_person", "number_of_years" ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
               "type": "object",
               "required": [
                  "name_of_person",
                  "number_of_years"
               ],
               "properties": {
                  "name_of_person": {
                     "type": "string"
                  },
                  "number_of_years": {
                     "type": "integer",
                     "minimum": -2147483648,
                     "maximum": 2147483647
                  }
               }
            }
            """.trimIndent()
    }

    val renamePropertiesKebabCase = case(
        "misc",
        "rename properties (kebab case) - https://github.com/SMILEY4/schema-kenerator/issues/18"
    ) {
        type = typeOf<TestClassIssue18>()
        postAnalyze = { this.renameMembers(JsonNamingStrategy.KebabCase) }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "name-of-person" : {
                      "type" : "string"
                    },
                    "number-of-years" : {
                      "type" : "integer",
                      "format" : "int32"
                    }
                  },
                  "required" : [ "name-of-person", "number-of-years" ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
               "type": "object",
               "required": [
                  "name-of-person",
                  "number-of-years"
               ],
               "properties": {
                  "name-of-person": {
                     "type": "string"
                  },
                  "number-of-years": {
                     "type": "integer",
                     "minimum": -2147483648,
                     "maximum": 2147483647
                  }
               }
            }
            """.trimIndent()
    }


    @Serializable
    data class TestClassIssue18(
        val nameOfPerson: String,
        val numberOfYears: Int
    )


    val customizePropertyWithSharedType = case(
        "misc",
        "customize property with shared type"
    ) {
        type = typeOf<TestClassCustomizeProperties>()
        postGenerateJsonSchema = {
            this.customizeProperties { propertyData, propertySchema ->
                if (propertyData.name == "describeMe" && propertySchema is JsonObject) {
                    propertySchema.properties["description"] = JsonTextValue("test description")
                }
            }
        }
        postGenerateSwaggerSchema = {
            this.customizeProperties { propertyData, propertySchema ->
                if (propertyData.name == "describeMe") {
                    propertySchema.description = "test description"
                }
            }
        }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "describeMe" : {
                      "type" : "string",
                      "description" : "test description"
                    },
                    "otherProperty" : {
                      "type" : "string"
                    }
                  },
                  "required" : [ "describeMe", "otherProperty" ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
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


    @Serializable
    class TestClassCustomizeProperties(
        val describeMe: String,
        val otherProperty: String
    )


    val nullablePropertyOfSealedClass = case(
        "misc",
        "nullable property of sealed class - https://github.com/SMILEY4/schema-kenerator/issues/39"
    ) {
        type = typeOf<TestClassIssue39b>()
        // language=json
        expectedSwaggerInline = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "a" : {
                      "anyOf" : [
                        {
                          "type" : "object",
                          "properties" : { }
                        },
                        {
                          "type" : "null"
                        }
                      ]
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
                  "type" : "object",
                  "properties" : {
                    "a" : {
                      "oneOf" : [
                        {
                          "type" : "null"
                        },
                        {
                          "${'$'}ref" : "#/components/schemas/SealedTestClassIssue39"
                        }
                      ]
                    }
                  }
                },
                "SealedTestClassIssue39" : {
                  "anyOf" : [ {
                    "${'$'}ref" : "#/components/schemas/TestClassIssue39a"
                  } ]
                },
                "TestClassIssue39a" : {
                  "type" : "object",
                  "properties" : { }
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJsonInline = """
            {
               "type": "object",
               "required": [],
               "properties": {
                  "a": {
                     "anyOf": [
                        {
                           "type": "object",
                           "required": [],
                           "properties": {}
                        },
                        {
                          "type" : "null"
                        }
                     ]
                  }
               }
            }
            """.trimIndent()
        // language=json
        expectedJsonReference = """
            {
               "type": "object",
               "required": [],
               "properties": {
                  "a": {
                     "oneOf": [
                        {
                           "type": "null"
                        },
                        {
                           "${'$'}ref": "#/${'$'}defs/SealedTestClassIssue39"
                        }
                     ]
                  }
               },
               "${'$'}defs": {
                  "SealedTestClassIssue39": {
                     "anyOf": [
                        {
                           "${'$'}ref": "#/${'$'}defs/TestClassIssue39a"
                        }
                     ]
                  },
                  "TestClassIssue39a": {
                     "type": "object",
                     "required": [],
                     "properties": {}
                  }
               }
            }
            """.trimIndent()
    }


    @Serializable
    sealed class SealedTestClassIssue39


    @Serializable
    class TestClassIssue39a : SealedTestClassIssue39()


    @Serializable
    class TestClassIssue39b(val a: SealedTestClassIssue39?)


    val mergePropertyAttributesIntoType = case(
        "misc",
        "merge property attributes into referenced type"
    ) {
        type = typeOf<TestClassMergePropertyAttributesIntoType>()
        postGenerateSwaggerSchema = {
            this
                .handleCoreAnnotations()
                .mergePropertyAttributesIntoType()
        }
        postGenerateJsonSchema = {
            this
                .handleCoreAnnotations()
                .mergePropertyAttributesIntoType()
        }
        // language=json
        expectedSwaggerInline = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "fieldA" : {
                      "type" : "type-a",
                      "format" : "format-a",
                      "properties" : {
                        "nameOfPerson" : {
                          "type" : "string"
                        },
                        "numberOfYears" : {
                          "type" : "integer",
                          "format" : "int32"
                        }
                      },
                      "required" : [ "nameOfPerson", "numberOfYears" ]
                    },
                    "fieldB" : {
                      "type" : "type-b",
                      "format" : "format-b",
                      "properties" : {
                        "nameOfPerson" : {
                          "type" : "string"
                        },
                        "numberOfYears" : {
                          "type" : "integer",
                          "format" : "int32"
                        }
                      },
                      "required" : [ "nameOfPerson", "numberOfYears" ]
                    }
                  },
                  "required" : [ "fieldA", "fieldB" ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedSwaggerReference = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "fieldA" : {
                      "${'$'}ref" : "#/components/schemas/TestClassMergePropertyAttributesIntoTypeNested"
                    },
                    "fieldB" : {
                      "${'$'}ref" : "#/components/schemas/TestClassMergePropertyAttributesIntoTypeNested2"
                    }
                  },
                  "required" : [ "fieldA", "fieldB" ]
                },
                "TestClassMergePropertyAttributesIntoTypeNested" : {
                  "type" : "type-a",
                  "format" : "format-a",
                  "properties" : {
                    "nameOfPerson" : {
                      "type" : "string"
                    },
                    "numberOfYears" : {
                      "type" : "integer",
                      "format" : "int32"
                    }
                  },
                  "required" : [ "nameOfPerson", "numberOfYears" ]
                },
                "TestClassMergePropertyAttributesIntoTypeNested2" : {
                  "type" : "type-b",
                  "format" : "format-b",
                  "properties" : {
                    "nameOfPerson" : {
                      "type" : "string"
                    },
                    "numberOfYears" : {
                      "type" : "integer",
                      "format" : "int32"
                    }
                  },
                  "required" : [ "nameOfPerson", "numberOfYears" ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJsonInline = """
            {
               "type": "object",
               "required": [
                  "fieldA",
                  "fieldB"
               ],
               "properties": {
                  "fieldA": {
                     "type": "type-a",
                     "required": [
                        "nameOfPerson",
                        "numberOfYears"
                     ],
                     "properties": {
                        "nameOfPerson": {
                           "type": "string"
                        },
                        "numberOfYears": {
                           "type": "integer",
                           "minimum": -2147483648,
                           "maximum": 2147483647
                        }
                     },
                     "format": "format-a"
                  },
                  "fieldB": {
                     "type": "type-b",
                     "required": [
                        "nameOfPerson",
                        "numberOfYears"
                     ],
                     "properties": {
                        "nameOfPerson": {
                           "type": "string"
                        },
                        "numberOfYears": {
                           "type": "integer",
                           "minimum": -2147483648,
                           "maximum": 2147483647
                        }
                     },
                     "format": "format-b"
                  }
               }
            }
            """.trimIndent()
        // language=json
        expectedJsonReference = """
            {
               "type": "object",
               "required": [
                  "fieldA",
                  "fieldB"
               ],
               "properties": {
                  "fieldA": {
                     "${'$'}ref": "#/${'$'}defs/TestClassMergePropertyAttributesIntoTypeNested"
                  },
                  "fieldB": {
                     "${'$'}ref": "#/${'$'}defs/TestClassMergePropertyAttributesIntoTypeNested2"
                  }
               },
               "${'$'}defs": {
                  "TestClassMergePropertyAttributesIntoTypeNested": {
                     "type": "type-a",
                     "format": "format-a",
                     "required": [
                        "nameOfPerson",
                        "numberOfYears"
                     ],
                     "properties": {
                        "nameOfPerson": {
                           "type": "string"
                        },
                        "numberOfYears": {
                           "type": "integer",
                           "minimum": -2147483648,
                           "maximum": 2147483647
                        }
                     }
                  },
                  "TestClassMergePropertyAttributesIntoTypeNested2": {
                     "type": "type-b",
                     "format": "format-b",
                     "required": [
                        "nameOfPerson",
                        "numberOfYears"
                     ],
                     "properties": {
                        "nameOfPerson": {
                           "type": "string"
                        },
                        "numberOfYears": {
                           "type": "integer",
                           "minimum": -2147483648,
                           "maximum": 2147483647
                        }
                     }
                  }
               }
            }
            """.trimIndent()
    }


    @Serializable
    data class TestClassMergePropertyAttributesIntoType(
        @Format("format-a")
        @Type("type-a")
        val fieldA: TestClassMergePropertyAttributesIntoTypeNested,
        @Format("format-b")
        @Type("type-b")
        val fieldB: TestClassMergePropertyAttributesIntoTypeNested
    )


    @Serializable
    data class TestClassMergePropertyAttributesIntoTypeNested(
        val nameOfPerson: String,
        val numberOfYears: Int
    )


    val kotlinxContextualFromConfig = case(
        "misc",
        "kotlinx contextual from serializer module config"
    ) {
        type = typeOf<TestClassContextualFromConfig>()
        withReflection = false
        kotlinxSerializationConfig = {
            serializersModule = SerializersModule {
                contextual(UUID::class, MyUUIDSerializer)
                contextual(Instant::class, MyInstantSerializer)
            }
        }
        postGenerateJsonSchema = { this.withTitle(JsonTitleType.MINIMAL) }
        postGenerateSwaggerSchema = { this.withTitle(SwaggerTitleType.MINIMAL) }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "timestamp" : {
                      "type" : "integer",
                      "format" : "int64",
                      "title" : "Instant"
                    },
                    "id" : {
                      "type" : "string",
                      "title" : "UUID"
                    }
                  },
                  "required" : [ "id", "timestamp" ],
                  "title" : "TestClassContextualFromConfig"
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
               "type": "object",
               "required": [
                  "timestamp",
                  "id"
               ],
               "properties": {
                  "timestamp": {
                     "type": "integer",
                     "minimum": -9223372036854775808,
                     "maximum": 9223372036854775807,
                     "title": "Instant"
                  },
                  "id": {
                     "type": "string",
                     "title": "UUID"
                  }
               },
               "title": "TestClassContextualFromConfig"
            }
            """.trimIndent()
    }


    @Serializable
    data class TestClassContextualFromConfig(
        @Contextual
        val timestamp: Instant,
        @Contextual
        val id: UUID,
    )


    val kotlinxContextualFromAnnotationWith = case(
        "misc",
        "kotlinx contextual from annotation ('with=...')"
    ) {
        type = typeOf<TestClassContextualFromAnnotationWith>()
        withReflection = false
        postGenerateJsonSchema = { this.withTitle(JsonTitleType.MINIMAL) }
        postGenerateSwaggerSchema = { this.withTitle(SwaggerTitleType.MINIMAL) }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "timestamp" : {
                      "type" : "integer",
                      "format" : "int64",
                      "title" : "Instant"
                    },
                    "id" : {
                      "type" : "string",
                      "title" : "UUID"
                    }
                  },
                  "required" : [ "id", "timestamp" ],
                  "title" : "TestClassContextualFromAnnotationWith"
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
               "type": "object",
               "required": [
                  "timestamp",
                  "id"
               ],
               "properties": {
                  "timestamp": {
                     "type": "integer",
                     "minimum": -9223372036854775808,
                     "maximum": 9223372036854775807,
                     "title": "Instant"
                  },
                  "id": {
                     "type": "string",
                     "title": "UUID"
                  }
               },
               "title": "TestClassContextualFromAnnotationWith"
            }
            """.trimIndent()
    }


    @Serializable
    data class TestClassContextualFromAnnotationWith(
        @Serializable(with = MyInstantSerializer::class)
        val timestamp: Instant,
        @Serializable(with = MyUUIDSerializer::class)
        val id: UUID,
    )

    val kotlinxMultipleContextualAnnotationsSameType = case(
        "misc",
        "kotlinx multiple contextual annotations with same type"
    ) {
        type = typeOf<ClassMultipleContextuals>()
        withReflection = false
        kotlinxSerializationConfig = {
            serializersModule = SerializersModule {
                contextual(MyInstantSerializer)
            }
        }
        postGenerateJsonSchema = { this.withTitle(JsonTitleType.MINIMAL) }
        postGenerateSwaggerSchema = { this.withTitle(SwaggerTitleType.MINIMAL) }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "fieldA" : {
                      "type" : [ "null", "integer" ],
                      "format" : "int64",
                      "title" : "Instant"
                    },
                    "fieldB" : {
                      "type" : [ "null", "integer" ],
                      "format" : "int64",
                      "title" : "Instant"
                    }
                  },
                  "title" : "ClassMultipleContextuals"
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
                  "fieldA": {
                     "type" : [ "null", "integer" ],
                     "minimum": -9223372036854775808,
                     "maximum": 9223372036854775807,
                     "title": "Instant"
                  },
                  "fieldB": {
                     "type" : [ "null", "integer" ],
                     "minimum": -9223372036854775808,
                     "maximum": 9223372036854775807,
                     "title": "Instant"
                  }
               },
               "title": "ClassMultipleContextuals"
            }
            """.trimIndent()
    }


    @Serializable
    data class ClassMultipleContextuals(
        val fieldA: @Contextual Instant? = null,
        val fieldB: @Contextual Instant? = null,
    )


    val overwritingTypeWithMoreSpecificType = case(
        "misc",
        "overwriting inherited type with more specific type - https://github.com/SMILEY4/schema-kenerator/issues/43"
    ) {
        type = typeOf<Issue43IntHolder>()
        postGenerateJsonSchema = { this.withTitle(JsonTitleType.MINIMAL) }
        postGenerateSwaggerSchema = { this.withTitle(SwaggerTitleType.MINIMAL) }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "value" : {
                      "type" : "integer",
                      "format" : "int32",
                      "title" : "Int"
                    }
                  },
                  "required" : [ "value" ],
                  "title" : "Issue43IntHolder"
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = """
            {
               "type": "object",
               "required": [
                  "value"
               ],
               "properties": {
                  "value": {
                     "type": "integer",
                     "minimum": -2147483648,
                     "maximum": 2147483647,
                     "title": "Int"
                  }
               },
               "title": "Issue43IntHolder"
            }
            """.trimIndent()
    }


    @Serializable
    data class Issue43IntHolder(
        override val value: Int
    ) : Issue43SimpleInterface


    @Serializable
    sealed interface Issue43SimpleInterface {
        val value: Number
    }

    val collectCorrectSubtypesWithTypeParametersInvolved = case(
        "misc",
        "collect correct subtypes with type parameters involved - https://github.com/SMILEY4/schema-kenerator/issues/43"
    ) {
        type = typeOf<Issue43Root>()
        postGenerateJsonSchema = { this.withTitle(JsonTitleType.MINIMAL) }
        postGenerateSwaggerSchema = { this.withTitle(SwaggerTitleType.MINIMAL) }
        // language=json
        expectedSwaggerInline = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "withEnum" : {
                      "type" : [ "null", "object" ],
                      "properties" : {
                        "data" : {
                          "type" : "string",
                          "enum" : [ "Alpha", "Beta" ],
                          "title" : "Issue43Enum"
                        }
                      },
                      "required" : [ "data" ],
                      "title" : "WithEnum"
                    },
                    "withInt" : {
                      "type" : [ "null", "object" ],
                      "properties" : {
                        "data" : {
                          "type" : "integer",
                          "format" : "int32",
                          "title" : "Int"
                        }
                      },
                      "required" : [ "data" ],
                      "title" : "WithInt"
                    }
                  },
                  "title" : "Issue43Root"
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedSwaggerReference = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "withEnum" : {
                      "oneOf" : [ {
                        "type" : "null"
                      }, {
                        "${'$'}ref" : "#/components/schemas/WithEnum"
                      } ]
                    },
                    "withInt" : {
                      "oneOf" : [ {
                        "type" : "null"
                      }, {
                        "${'$'}ref" : "#/components/schemas/WithInt"
                      } ]
                    }
                  },
                  "title" : "Issue43Root"
                },
                "WithEnum" : {
                  "type" : "object",
                  "properties" : {
                    "data" : {
                      "${'$'}ref" : "#/components/schemas/Issue43Enum"
                    }
                  },
                  "required" : [ "data" ],
                  "title" : "WithEnum"
                },
                "Issue43Enum" : {
                  "type" : "string",
                  "enum" : [ "Alpha", "Beta" ],
                  "title" : "Issue43Enum"
                },
                "WithInt" : {
                  "type" : "object",
                  "properties" : {
                    "data" : {
                      "type" : "integer",
                      "format" : "int32",
                      "title" : "Int"
                    }
                  },
                  "required" : [ "data" ],
                  "title" : "WithInt"
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJsonInline = """
            {
               "type": "object",
               "required": [],
               "properties": {
                  "withEnum": {
                     "type" : [ "null", "object" ],
                     "required": [
                        "data"
                     ],
                     "properties": {
                        "data": {
                           "enum": [
                              "Alpha",
                              "Beta"
                           ],
                           "title": "Issue43Enum"
                        }
                     },
                     "title": "WithEnum"
                  },
                  "withInt": {
                     "type" : [ "null", "object" ],
                     "required": [
                        "data"
                     ],
                     "properties": {
                        "data": {
                           "type": "integer",
                           "minimum": -2147483648,
                           "maximum": 2147483647,
                           "title": "Int"
                        }
                     },
                     "title": "WithInt"
                  }
               },
               "title": "Issue43Root"
            }
            """.trimIndent()
        // language=json
        expectedJsonReference = """
            {
               "type": "object",
               "required": [],
               "properties": {
                 "withEnum" : {
                   "oneOf" : [ {
                     "type" : "null"
                   }, {
                     "${'$'}ref": "#/${'$'}defs/WithEnum"
                   } ]
                 },
                 "withInt" : {
                   "oneOf" : [ {
                     "type" : "null"
                   }, {
                     "${'$'}ref": "#/${'$'}defs/WithInt"
                   } ]
                 }
               },
               "title": "Issue43Root",
               "${'$'}defs": {
                  "WithEnum": {
                     "type": "object",
                     "required": [
                        "data"
                     ],
                     "properties": {
                        "data": {
                           "${'$'}ref": "#/${'$'}defs/Issue43Enum"
                        }
                     },
                     "title": "WithEnum"
                  },
                  "Issue43Enum": {
                     "enum": [
                        "Alpha",
                        "Beta"
                     ],
                     "title": "Issue43Enum"
                  },
                  "WithInt": {
                     "type": "object",
                     "required": [
                        "data"
                     ],
                     "properties": {
                        "data": {
                           "type": "integer",
                           "minimum": -2147483648,
                           "maximum": 2147483647,
                           "title": "Int"
                        }
                     },
                     "title": "WithInt"
                  }
               }
            }
            """.trimIndent()
    }


    @Serializable
    data class Issue43Root(
        val withInt: Issue42Interface.WithInt?,
        val withEnum: Issue42Interface.WithEnum?,
    )


    @Serializable
    sealed interface Issue42Interface<T> {
        val data: T


        @Serializable
        data class WithInt(override val data: Int) : Issue42Interface<Int>


        @Serializable
        data class WithEnum(override val data: Issue43Enum) : Issue42Interface<Issue43Enum>
    }

    enum class Issue43Enum { Alpha, Beta, }

    val specialFloatingPointValuesDontAllow = case(
        "misc",
        "special floating point values - don't allow"
    ) {
        type = typeOf<TestClassSpecialFloatingPointValues>()
        swaggerConfig = {
            allowSpecialFloatingPointValues = false
        }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "doubleValue" : {
                      "type" : "number",
                      "format" : "double"
                    },
                    "floatValue" : {
                      "type" : "number",
                      "format" : "float"
                    },
                    "intValue" : {
                      "type" : "integer",
                      "format" : "int32"
                    }
                  },
                  "required" : [ "doubleValue", "floatValue", "intValue" ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJson = null // not supported for json schemas
    }

    val specialFloatingPointValuesAllow = case(
        "misc",
        "special floating point values - allow"
    ) {
        type = typeOf<TestClassSpecialFloatingPointValues>()
        swaggerConfig = {
            allowSpecialFloatingPointValues = true
        }
        expectedSwagger = """
           {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "doubleValue" : {
                      "anyOf" : [ {
                        "type" : "number",
                        "format" : "double"
                      }, {
                        "enum" : [ "NaN", "Infinity", "-Infinity" ]
                      } ]
                    },
                    "floatValue" : {
                      "anyOf" : [ {
                        "type" : "number",
                        "format" : "float"
                      }, {
                        "enum" : [ "NaN", "Infinity", "-Infinity" ]
                      } ]
                    },
                    "intValue" : {
                      "type" : "integer",
                      "format" : "int32"
                    }
                  },
                  "required" : [ "doubleValue", "floatValue", "intValue" ]
                }
              }
            }
            """.trimIndent()
        expectedJson = null // not supported for json schemas
    }


    @Serializable
    data class TestClassSpecialFloatingPointValues(
        val floatValue: Float,
        val doubleValue: Double,
        val intValue: Int,
    )

    val mapsWithComplexKeysAsArraysDisabled = case(
        "misc",
        "maps with complex keys as array - disabled"
    ) {
        type = typeOf<TestClassWithCombinedKeyMap>()
        swaggerConfig = {
            mapsWithStructuredKeysAsArrays = false
        }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "map" : {
                      "type" : "object",
                      "additionalProperties" : {
                        "type" : "integer",
                        "format" : "int32"
                      }
                    }
                  },
                  "required" : [ "map" ]
                }
              }
            }
            """.trimIndent()
        expectedJson = null // not supported for json schemas
    }

    val mapsWithComplexKeysAsArraysEnabled = case(
        "misc",
        "maps with complex keys as array - enabled"
    ) {
        type = typeOf<TestClassWithCombinedKeyMap>()
        swaggerConfig = {
            mapsWithStructuredKeysAsArrays = true
        }
        // language=json
        expectedSwaggerInline = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "map" : {
                      "type" : "array",
                      "items" : {
                        "anyOf" : [ {
                          "type" : "object",
                          "properties" : {
                            "a" : {
                              "type" : "string"
                            },
                            "b" : {
                              "type" : "integer",
                              "format" : "int32"
                            }
                          },
                          "required" : [ "a", "b" ]
                        }, {
                          "type" : "integer",
                          "format" : "int32"
                        } ]
                      }
                    }
                  },
                  "required" : [ "map" ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedSwaggerReference = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "map" : {
                      "type" : "array",
                      "items" : {
                        "anyOf" : [ {
                          "${'$'}ref" : "#/components/schemas/CombinedKey"
                        }, {
                          "type" : "integer",
                          "format" : "int32"
                        } ]
                      }
                    }
                  },
                  "required" : [ "map" ]
                },
                "CombinedKey" : {
                  "type" : "object",
                  "properties" : {
                    "a" : {
                      "type" : "string"
                    },
                    "b" : {
                      "type" : "integer",
                      "format" : "int32"
                    }
                  },
                  "required" : [ "a", "b" ]
                }
              }
            }
            """.trimIndent()
        expectedJson = null // not supported for json schemas
    }


    @Serializable
    data class TestClassWithCombinedKeyMap(
        val map: Map<CombinedKey, Int>
    )


    @Serializable
    data class CombinedKey(val a: String, val b: Int)


    val descriptionOnPropertyAndType = case(
        "misc",
        "description annotations on property and type"
    ) {
        type = typeOf<TestClassWithPropertyDescription>()
        postGenerateSwaggerSchema = {
            this
                .handleCoreAnnotations()
                .mergePropertyAttributesIntoType()
        }
        postGenerateJsonSchema = {
            this
                .handleCoreAnnotations()
                .mergePropertyAttributesIntoType()
        }
        // language=json
        expectedSwaggerInline = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "someProp" : {
                      "type" : "object",
                      "description" : "description on property",
                      "properties" : {
                        "value" : {
                          "type" : "integer",
                          "format" : "int32"
                        }
                      },
                      "required" : [ "value" ]
                    }
                  },
                  "required" : [ "someProp" ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedSwaggerReference = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "someProp" : {
                      "${'$'}ref" : "#/components/schemas/TestClassWithDescription",
                      "description" : "description on property"
                    }
                  },
                  "required" : [ "someProp" ]
                },
                "TestClassWithDescription" : {
                  "type" : "object",
                  "description" : "description on class",
                  "properties" : {
                    "value" : {
                      "type" : "integer",
                      "format" : "int32"
                    }
                  },
                  "required" : [ "value" ]
                }
              }
            }
            """.trimIndent()
        // language=json
        expectedJsonInline = """
            {
               "type": "object",
               "required": [
                  "someProp"
               ],
               "properties": {
                  "someProp": {
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
                     },
                     "description": "description on property"
                  }
               }
            }
            """.trimIndent()
        // language=json
        expectedJsonReference = """
            {
               "type": "object",
               "required": [
                  "someProp"
               ],
               "properties": {
                  "someProp": {
                     "${'$'}ref": "#/${'$'}defs/TestClassWithDescription",
                     "description": "description on property"
                  }
               },
               "${'$'}defs": {
                  "TestClassWithDescription": {
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
                     },
                     "description": "description on class"
                  }
               }
            }
            """.trimIndent()
    }


    @Serializable
    class TestClassWithPropertyDescription(
        @Description("description on property") val someProp: TestClassWithDescription
    )


    @Serializable
    @Description("description on class")
    class TestClassWithDescription(
        val value: Int
    )

    val propertyNullableSwaggerSchemaRequired = case(
        "misc",
        "nullable property marked required by (swagger) schema annotation"
    ) {
        type = typeOf<TestClassNullablePropWithSchemaRequired>()
        postGenerateSwaggerSchema = {
            this.handleSchemaAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "someText" : {
                      "type" : [ "null", "string" ]
                    }
                  },
                  "required" : [ "someText" ]
                }
              }
            }
            """.trimIndent()
        // swagger @Schema not supported by kotlinx-serialization
        // language=json
        expectedSwaggerKotlinxSerialization = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "someText" : {
                      "type" : [ "null", "string" ]
                    }
                  }
                }
              }
            }
            """.trimIndent()
        expectedJsonInline = null
        expectedJsonReference = null
    }

    val propertyNullableCoreRequired = case(
        "misc",
        "nullable property marked required by (core) required annotation"
    ) {
        type = typeOf<TestClassNullablePropWithCoreRequired>()
        postGenerateSwaggerSchema = {
            this.handleCoreAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas" : {
                "_root" : {
                  "type" : "object",
                  "properties" : {
                    "someText" : {
                      "type" : [ "null", "string" ]
                    }
                  },
                  "required" : [ "someText" ]
                }
              }
            }
            """.trimIndent()
        expectedJsonInline = null
        expectedJsonReference = null
    }

    @Serializable
    data class TestClassNullablePropWithSchemaRequired(
        @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        val someText: String?,
    )

    @Serializable
    data class TestClassNullablePropWithCoreRequired(
        @Required
        val someText: String?,
    )

    object MyInstantSerializer : KSerializer<Instant> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("java.time.Instant", PrimitiveKind.LONG)
        override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeLong(value.toEpochMilli())
        override fun deserialize(decoder: Decoder): Instant = Instant.ofEpochMilli(decoder.decodeLong())
    }

    object MyUUIDSerializer : KSerializer<UUID> {
        override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
    }

}