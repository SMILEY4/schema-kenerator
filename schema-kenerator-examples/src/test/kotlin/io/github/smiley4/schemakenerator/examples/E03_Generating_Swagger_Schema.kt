@file:Suppress("ClassName")

package io.github.smiley4.schemakenerator.examples

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.annotations.Default
import io.github.smiley4.schemakenerator.core.annotations.Deprecated
import io.github.smiley4.schemakenerator.core.annotations.Description
import io.github.smiley4.schemakenerator.core.annotations.Example
import io.github.smiley4.schemakenerator.core.annotations.Title
import io.github.smiley4.schemakenerator.examples.E03_Generating_JsonSchema.Companion.SimpleClass
import io.github.smiley4.schemakenerator.jackson.swagger.handleJacksonSwaggerAnnotations
import io.github.smiley4.schemakenerator.swagger.OptionalHandling
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.compileReferencing
import io.github.smiley4.schemakenerator.swagger.compileReferencingRoot
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleCoreAnnotations
import io.github.smiley4.schemakenerator.swagger.handleSchemaAnnotations
import io.github.smiley4.schemakenerator.swagger.withTitle
import io.github.smiley4.schemakenerator.validation.swagger.handleJakartaValidationAnnotations
import io.github.smiley4.schemakenerator.validation.swagger.handleJavaxValidationAnnotations
import io.kotest.core.spec.style.FreeSpec
import io.swagger.util.Json
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import kotlin.reflect.typeOf

class E03_Generating_Swagger_Schema : FreeSpec({

    "basic schema generation" - {
        // after extracting and modifying data of classes and types, the json-schema is created with the steps:
        // 1. "generateJsonSchema"-step: generates an independent json-schema for each processed type. At this stage, the schemas may not be 100% valid.
        // 2. "compile[...]"-step: this step takes the independent json-schemas created by the generateJsonSchema-step and merges them into one or multiple final valid schemas

        "inline" {

            // the "compileInlining"-step takes all individual schemas and merges them into one single swagger-schema by inlining all referenced types.

            val swaggerSchema = typeOf<ExampleClass>()
                .processReflection()
                .generateSwaggerSchema()
                .compileInlining()

            println(Json.pretty(swaggerSchema.swagger))
            // {
            //   "type" : "object",
            //   "required" : [ "nested", "number" ],
            //   "properties" : {
            //     "nested" : {
            //       "type" : "object",
            //       "required" : [ "flag" ],
            //       "properties" : {
            //         "flag" : {
            //           "type" : "boolean",
            //         }
            //       },
            //     },
            //     "number" : {
            //       "type" : "integer",
            //       "format" : "int32",
            //     },
            //     "text" : {
            //       "types" : [ "string", "null" ]
            //     }
            //   }
            // }
        }

        "referencing" {

            // the "compileReferencing"-step takes all individual schemas and only inlines all primitive or simple schemas. All other schemas are referenced.

            val swaggerSchema = typeOf<ExampleClass>()
                .processReflection()
                .generateSwaggerSchema()
                .compileReferencing()

            println(Json.pretty(swaggerSchema.swagger))
            // {
            //   "type" : "object",
            //   "required" : [ "nested", "number" ],
            //   "properties" : {
            //     "nested" : {
            //       "$ref" : "#/components/schemas/io.github.smiley4.schemakenerator.examples.E03_Generating_Swagger_Schema.Companion.NestedClass",
            //     },
            //     "number" : {
            //       "type" : "integer",
            //       "format" : "int32",
            //     },
            //     "text" : {
            //       "types" : [ "string", "null" ]
            //     }
            //   }
            // }

            println(Json.pretty(swaggerSchema.componentSchemas["io.github.smiley4.schemakenerator.examples.E03_Generating_Swagger_Schema.Companion.NestedClass"]))
            // {
            //   "type" : "object",
            //   "required" : [ "flag" ],
            //   "properties" : {
            //     "flag" : {
            //       "type" : "boolean",
            //     }
            //   }
            // }

        }

        "referencing (including root schema)" {

            // the "compileReferencingRoot"-step takes all individual schemas and only inlines all primitive or simple schemas. All other schemas including the original root schema are referenced.

            val swaggerSchema = typeOf<ExampleClass>()
                .processReflection()
                .generateSwaggerSchema()
                .compileReferencingRoot()

            println(Json.pretty(swaggerSchema.swagger))
            // {
            //   "$ref" : "#/components/schemas/io.github.smiley4.schemakenerator.examples.E03_Generating_Swagger_Schema.Companion.ExampleClass"
            // }

            println(Json.pretty(swaggerSchema.componentSchemas["io.github.smiley4.schemakenerator.examples.E03_Generating_Swagger_Schema.Companion.ExampleClass"]))
            // {
            //   "type" : "object",
            //   "required" : [ "nested", "number" ],
            //   "properties" : {
            //     "nested" : {
            //       "$ref" : "#/components/schemas/io.github.smiley4.schemakenerator.examples.E03_Generating_Swagger_Schema.Companion.NestedClass",
            //     },
            //     "number" : {
            //       "type" : "integer",
            //       "format" : "int32",
            //     },
            //     "text" : {
            //       "types" : [ "string", "null" ]
            //     }
            //   }
            // }

            println(Json.pretty(swaggerSchema.componentSchemas["io.github.smiley4.schemakenerator.examples.E03_Generating_Swagger_Schema.Companion.NestedClass"]))
            // {
            //   "type" : "object",
            //   "required" : [ "flag" ],
            //   "properties" : {
            //     "flag" : {
            //       "type" : "boolean",
            //     }
            //   }
            // }

        }

    }

    "configuring the generation step" {

        // The behavior and output of the "generateJsonSchema()"-step can be configured:

        typeOf<SimpleClass>()
            .processReflection()
            .generateSwaggerSchema {
                // configure how optional properties, i.e properties that are not necessarily nullable but have a default value assigned, should be treated
                // "REQUIRED":     these properties are handled as "required"
                // "NON_REQUIRED": these properties are handled as "not required"
                optionalHandling = OptionalHandling.REQUIRED
            }

    }

    "reference path type" - {

        // schemas can be referenced using their simple, full name or an openapi-compatible version of both

        "simple openapi name" {

            val swaggerSchema = typeOf<List<GenericClass<String>>>()
                .processReflection()
                .generateSwaggerSchema()
                .compileReferencing(pathType = RefType.OPENAPI_SIMPLE) // "OPENAPI_SIMPLE" takes the simple/short name of the type for the reference path and modifies it to be compatible with openapi-spec.

            println(Json.pretty(swaggerSchema.swagger))
            // {
            //   "type" : "array",
            //   "items" : {
            //     "$ref" : "#/components/schemas/GenericClass_String",
            //   }
            // }
        }

        "full openapi name" {

            val swaggerSchema = typeOf<List<GenericClass<String>>>()
                .processReflection()
                .generateSwaggerSchema()
                .compileReferencing(pathType = RefType.OPENAPI_FULL) // "OPENAPI_FULL" takes the full/qualified name of the type for the reference path and modifies it to be compatible with openapi-spec.

            println(Json.pretty(swaggerSchema.swagger))
            // {
            //   "type" : "array",
            //   "items" : {
            //     "$ref" : "#/components/schemas/io.github.smiley4.schemakenerator.examples.E03_Generating_Swagger_Schema.Companion.GenericClass_kotlin.String",
            //   }
            // }
        }

        "simple name" {

            val swaggerSchema = typeOf<List<GenericClass<String>>>()
                .processReflection()
                .generateSwaggerSchema()
                .compileReferencing(pathType = RefType.SIMPLE) // "SIMPLE" takes the simple/short name of the type for the reference path. This name may not be compatible with openapi-spec.

            println(Json.pretty(swaggerSchema.swagger))
            // {
            //   "type" : "array",
            //   "items" : {
            //     "$ref" : "#/components/schemas/GenericClass<String>",
            //   }
            // }

        }

        "full name" {

            val swaggerSchema = typeOf<List<GenericClass<String>>>()
                .processReflection()
                .generateSwaggerSchema()
                .compileReferencing(pathType = RefType.FULL) // "FULL" takes the full/qualified name of the type for the reference path. This name may not be compatible with openapi-spec.

            println(Json.pretty(swaggerSchema.swagger))
            // {
            //   "type" : "array",
            //   "items" : {
            //     "$ref" : "#/components/schemas/io.github.smiley4.schemakenerator.examples.E03_Generating_Swagger_Schema.Companion.GenericClass<kotlin.String>",
            //   }
            // }
        }

    }

    "automatically adding a schema title" {

        // a "title" property can be automatically added to all types in the swagger-schema

        val swaggerSchema = typeOf<List<GenericClass<String>>>()
            .processReflection()
            .generateSwaggerSchema()
            .withTitle(type = TitleType.OPENAPI_SIMPLE)
            .compileInlining()

        // "TitleType.OPENAPI_SIMPLE" takes the simple/short name of the type as the title and modifies it to be compatible with openapi-spec.
        // "TitleType.OPENAPI_FULL" takes the full/qualified name of the type as the title and modifies it to be compatible with openapi-spec.
        // "TitleType.SIMPLE" takes the simple/short name of the type as the title. This name may not be compatible with openapi-spec.
        // "TitleType.FULL" takes the full/qualified name of the type for the reference path. This name may not be compatible with openapi-spec.

        println(Json.pretty(swaggerSchema.swagger))
        // {
        //   "title" : "List_GenericClass_String",
        //   "type" : "array",
        //   "items" : {
        //     "title" : "GenericClass_String",
        //     "type" : "object",
        //     "required" : [ "value" ],
        //     "properties" : {
        //       "value" : {
        //         "title" : "String",
        //         "type" : "string",
        //       }
        //     },
        //   }
        // }

    }

    "adding additional information with annotations" - {

        // properties of the swagger-schema can be filled with additional information from annotations, e.g. descriptions or default values

        "schema-kenerator-core annotations" {

            // the "handleCoreAnnotations()"-step adds information from annotations from schema-kenerator-core to the swagger-schema

            val swaggerSchema = typeOf<CoreAnnotatedClass>()
                .processReflection()
                .generateSwaggerSchema()
                .handleCoreAnnotations()
                .compileInlining()

            println(Json.pretty(swaggerSchema.swagger))
            // {
            //   "title" : "Annotated Class",
            //   "type" : "object",
            //   "description" : "some description",
            //   "example" : "example 1",
            //   "deprecated" : true,
            //   "default" : "default value",
            //   "required" : [ "intValue", "stringValue" ],
            //   "properties" : {
            //     "intValue" : {
            //       "type" : "integer",
            //       "description" : "Int field description",
            //       "format" : "int32",
            //       "example" : "2222",
            //       "default" : "1111"
            //     },
            //     "stringValue" : {
            //       "type" : "string",
            //       "description" : "String field description",
            //       "example" : "An example of a String value",
            //       "default" : "A default String value"
            //     }
            //   },
            // }

        }

        "swagger annotations" {

            // the "handleSchemaAnnotations()"-step adds information from swagger @Schema and @ArraySchema annotations to the swagger-schema

            val swaggerSchema = typeOf<SwaggerAnnotatedClass>()
                .processReflection()
                .generateSwaggerSchema()
                .handleSchemaAnnotations()
                .compileInlining()

            println(Json.pretty(swaggerSchema.swagger))
            // {
            //   "title" : "Swagger Annotated Class",
            //   "description" : "some description",
            //   "type" : "object",
            //   "required" : [ "myValue", "someTags" ],
            //   "properties" : {
            //     "myValue" : {
            //       "title" : "Some Value",
            //       "maximum" : 9,
            //       "exclusiveMaximum" : true,
            //       "minimum" : 0,
            //       "exclusiveMinimum" : false,
            //       "maxLength" : 10,
            //       "minLength" : 1,
            //       "type" : "integer",
            //       "description" : "some value",
            //       "format" : "single-digit",
            //       "readOnly" : true,
            //       "enum" : [ "1", "2", "3", "4" ],
            //       "default" : "1"
            //     },
            //     "someTags" : {
            //       "maxItems" : 10,
            //       "minItems" : 0,
            //       "uniqueItems" : true,
            //       "type" : "array",
            //       "items" : {
            //         "type" : "string",
            //       }
            //     }
            //   },
            // }

        }

        "jackson annotations" {

            // the "handleJacksonSwaggerAnnotations()"-step adds information from jackson annotations to the swagger-schema

            val swaggerSchema = typeOf<JacksonAnnotatedClass>()
                .processReflection()
                .generateSwaggerSchema()
                .handleJacksonSwaggerAnnotations()
                .compileInlining()

            println(Json.pretty(swaggerSchema.swagger))
            // {
            //   "required" : [ "value" ],
            //   "type" : "object",
            //   "properties" : {
            //     "value" : {
            //       "type" : "string",
            //       "description" : "Example description of the field",
            //       "exampleSetFlag" : false
            //     }
            //   },
            //   "exampleSetFlag" : false
            // }

        }

        "javax and jackarta validation annotations" {

            // the "handleJavaxValidationAnnotations()" and "handleJakartaValidationAnnotations()"-steps add
            // information from javax and jackarta validation annotations to the swagger-schema

            val swaggerSchema = typeOf<JavaxValidatedClass>()
                .processReflection()
                .generateSwaggerSchema()
                .handleJavaxValidationAnnotations()
                .handleJakartaValidationAnnotations()
                .compileInlining()

            println(Json.pretty(swaggerSchema.swagger))
            // {
            //   "type" : "object",
            //   "required" : [ "hasSize", "minMax", "mustNotBeBlank", "mustNotBeEmpty", "mustNotBeNull" ],
            //   "properties" : {
            //     "hasSize" : {
            //       "type" : "string",
            //       "maxLength" : 95,
            //       "minLength" : 4,
            //     },
            //     "minMax" : {
            //       "type" : "integer",
            //       "format" : "int32",
            //       "maximum" : 10,
            //       "minimum" : 5,
            //     },
            //     "mustNotBeBlank" : {
            //       "type" : "string",
            //     },
            //     "mustNotBeEmpty" : {
            //       "type" : "string",
            //     },
            //     "mustNotBeNull" : {
            //       "type" : "object",
            //     }
            //   },
            // }
        }

    }

}) {
    companion object {

        private val json = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()!!


        private class NestedClass(
            val flag: Boolean
        )


        private class ExampleClass(
            val nested: NestedClass,
            val number: Int,
            val text: String?,
        )


        private class GenericClass<T>(
            val value: T
        )


        @Title("Annotated Class")
        @Description("some description")
        @Default("default value")
        @Example("example 1")
        @Deprecated
        private class CoreAnnotatedClass(
            @Description("String field description")
            @Default("A default String value")
            @Example("An example of a String value")
            val stringValue: String,

            @Description("Int field description")
            @Default("1111")
            @Example("2222")
            val intValue: Int,
        )


        @Schema(
            title = "Swagger Annotated Class",
            name = "SwaggerAnnotatedClass",
            description = "some description",
        )
        private class SwaggerAnnotatedClass(

            @Schema(
                description = "some value",
                title = "Some Value",
                name = "someValue",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = ["1", "2", "3", "4"],
                defaultValue = "1",
                accessMode = Schema.AccessMode.READ_ONLY,
                minLength = 1,
                maxLength = 10,
                format = "single-digit",
                minimum = "0",
                maximum = "9",
                exclusiveMaximum = true
            )
            val myValue: Int,

            @ArraySchema(
                minItems = 0,
                maxItems = 10,
                uniqueItems = true
            )
            val someTags: List<String>
        )


        private class JacksonAnnotatedClass(
            @JsonPropertyDescription("Example description of the field")
            val value: String
        )


        private class JavaxValidatedClass(
            @Min(5)
            @Max(10)
            val minMax: Int,
            @NotNull
            val mustNotBeNull: Any?,
            @NotEmpty
            val mustNotBeEmpty: String?,
            @NotBlank
            val mustNotBeBlank: String?,
            @Size(min = 4, max = 95)
            val hasSize: String
        )
    }
}
