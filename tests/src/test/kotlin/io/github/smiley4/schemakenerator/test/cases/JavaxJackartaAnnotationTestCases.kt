package io.github.smiley4.schemakenerator.test.cases

import io.github.smiley4.schemakenerator.validation.swagger.ValidationSwaggerSteps.handleJakartaValidationAnnotations
import io.github.smiley4.schemakenerator.validation.swagger.ValidationSwaggerSteps.handleJavaxValidationAnnotations
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import kotlin.reflect.typeOf

object JavaxJackartaAnnotationTestCases {

    val validationsJavax = case("javax & jackarta annotations", "basic validations (javax)") {
        type = typeOf<JavaxValidated>()
        withKotlinxSerialization = false
        postGenerateSwaggerSchema = {
            this.handleJavaxValidationAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "hasSize": {
                      "type": "string",
                      "maxLength": 95,
                      "minLength": 4
                    },
                    "minMax": {
                      "type": "integer",
                      "format": "int32",
                      "maximum": 10,
                      "minimum": 5
                    },
                    "mustNotBeBlank": {
                      "type": "string"
                    },
                    "mustNotBeEmpty": {
                      "type": "string"
                    },
                    "mustNotBeNull": {
                      "type": "object"
                    }
                  },
                  "required": [
                    "hasSize",
                    "minMax",
                    "mustNotBeBlank",
                    "mustNotBeEmpty",
                    "mustNotBeNull"
                  ]
                }
              }
            }
            """.trimIndent()
        expectedJson = null
    }

    val validationsJackarta = case("javax & jackarta annotations", "basic validations (jackarta)") {
        type = typeOf<JakartaValidated>()
        withKotlinxSerialization = false
        postGenerateSwaggerSchema = {
            this.handleJakartaValidationAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "hasSize": {
                      "type": "string",
                      "maxLength": 95,
                      "minLength": 4
                    },
                    "minMax": {
                      "type": "integer",
                      "format": "int32",
                      "maximum": 10,
                      "minimum": 5
                    },
                    "mustNotBeBlank": {
                      "type": "string"
                    },
                    "mustNotBeEmpty": {
                      "type": "string"
                    },
                    "mustNotBeNull": {
                      "type": "object"
                    }
                  },
                  "required": [
                    "hasSize",
                    "minMax",
                    "mustNotBeBlank",
                    "mustNotBeEmpty",
                    "mustNotBeNull"
                  ]
                }
              }
            }
            """.trimIndent()
        expectedJson = null
    }

    val notNullJavax = case("javax & jackarta annotations", "not null for all properties (javax)") {
        type = typeOf<NotNullWithAllNullProperties>()
        withKotlinxSerialization = false
        postGenerateSwaggerSchema = {
            this.handleJavaxValidationAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "requiredProperty": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "requiredProperty"
                  ]
                }
              }
            }
            """.trimIndent()
        expectedJson = null
    }

    val notNullJackarta = case("javax & jackarta annotations", "not null for all properties (jackarta)") {
        type = typeOf<JakartaNotNullWithAllNullProperties>()
        withKotlinxSerialization = false
        postGenerateSwaggerSchema = {
            this.handleJakartaValidationAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "requiredProperty": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "requiredProperty"
                  ]
                }
              }
            }
            """.trimIndent()
        expectedJson = null
    }

    val notEmptyJavax = case("javax & jackarta annotations", "not empty for all properties (javax)") {
        type = typeOf<NotEmptyWithAllNullProperties>()
        withKotlinxSerialization = false
        postGenerateSwaggerSchema = {
            this.handleJavaxValidationAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "requiredProperty": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "requiredProperty"
                  ]
                }
              }
            }
            """.trimIndent()
        expectedJson = null
    }

    val notEmptyJackarta = case("javax & jackarta annotations", "not empty for all properties (jackarta)") {
        type = typeOf<JakartaNotEmptyWithAllNullProperties>()
        withKotlinxSerialization = false
        postGenerateSwaggerSchema = {
            this.handleJakartaValidationAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "requiredProperty": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "requiredProperty"
                  ]
                }
              }
            }
            """.trimIndent()
        expectedJson = null
    }

    val notBlankJavax = case("javax & jackarta annotations", "not blank for all properties (javax)") {
        type = typeOf<NotBlankWithAllNullProperties>()
        withKotlinxSerialization = false
        postGenerateSwaggerSchema = {
            this.handleJavaxValidationAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "requiredProperty": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "requiredProperty"
                  ]
                }
              }
            }
            """.trimIndent()
        expectedJson = null
    }

    val notBlankJackarta = case("javax & jackarta annotations", "not blank for all properties (jackarta)") {
        type = typeOf<JakartaNotBlankWithAllNullProperties>()
        withKotlinxSerialization = false
        postGenerateSwaggerSchema = {
            this.handleJakartaValidationAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "requiredProperty": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "requiredProperty"
                  ]
                }
              }
            }
            """.trimIndent()
        expectedJson = null
    }

    val allRequiredAnnotationsJavax = case("javax & jackarta annotations", "all required annotations for all properties (javax)") {
        type = typeOf<AllRequiredValidations>()
        withKotlinxSerialization = false
        postGenerateSwaggerSchema = {
            this.handleJavaxValidationAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "requiredProperty": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "requiredProperty"
                  ]
                }
              }
            }
            """.trimIndent()
        expectedJson = null
    }

    val allRequiredAnnotationsJackarta = case("javax & jackarta annotations", "all required annotations for all properties (jackarta)") {
        type = typeOf<JakartaAllRequiredValidations>()
        withKotlinxSerialization = false
        postGenerateSwaggerSchema = {
            this.handleJakartaValidationAnnotations()
        }
        // language=json
        expectedSwagger = """
            {
              "schemas": {
                "_root": {
                  "type": "object",
                  "properties": {
                    "requiredProperty": {
                      "type": "string"
                    }
                  },
                  "required": [
                    "requiredProperty"
                  ]
                }
              }
            }
            """.trimIndent()
        expectedJson = null
    }

    private class JavaxValidated(
        @field:Min(5)
        @field:Max(10)
        val minMax: Int,
        @field:NotNull
        val mustNotBeNull: Any?,
        @field:NotEmpty
        val mustNotBeEmpty: String?,
        @field:NotBlank
        val mustNotBeBlank: String?,
        @field:Size(min = 4, max = 95)
        val hasSize: String
    )

    private class JakartaValidated(
        @field:jakarta.validation.constraints.Min(5)
        @field:jakarta.validation.constraints.Max(10)
        val minMax: Int,
        @field:jakarta.validation.constraints.NotNull
        val mustNotBeNull: Any?,
        @field:jakarta.validation.constraints.NotEmpty
        val mustNotBeEmpty: String?,
        @field:jakarta.validation.constraints.NotBlank
        val mustNotBeBlank: String?,
        @field:jakarta.validation.constraints.Size(min = 4, max = 95)
        val hasSize: String
    )

    private class NotNullWithAllNullProperties(
        @field:NotNull
        val requiredProperty: String?
    )

    private class NotEmptyWithAllNullProperties(
        @field:NotEmpty
        val requiredProperty: String?
    )

    private class NotBlankWithAllNullProperties(
        @field:NotBlank
        val requiredProperty: String?
    )

    private class JakartaNotNullWithAllNullProperties(
        @field:jakarta.validation.constraints.NotNull
        val requiredProperty: String?
    )

    private class JakartaNotEmptyWithAllNullProperties(
        @field:jakarta.validation.constraints.NotEmpty
        val requiredProperty: String?
    )

    private class JakartaNotBlankWithAllNullProperties(
        @field:jakarta.validation.constraints.NotBlank
        val requiredProperty: String?
    )

    private class AllRequiredValidations(
        @field:NotNull
        @field:NotEmpty
        @field:NotBlank
        val requiredProperty: String?
    )

    private class JakartaAllRequiredValidations(
        @field:jakarta.validation.constraints.NotNull
        @field:jakarta.validation.constraints.NotEmpty
        @field:jakarta.validation.constraints.NotBlank
        val requiredProperty: String?
    )

}
