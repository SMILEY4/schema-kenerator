@file:Suppress("ClassName")

package io.github.smiley4.schemakenerator.examples

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import io.github.smiley4.schemakenerator.core.annotations.Default
import io.github.smiley4.schemakenerator.core.annotations.Deprecated
import io.github.smiley4.schemakenerator.core.annotations.Description
import io.github.smiley4.schemakenerator.core.annotations.Example
import io.github.smiley4.schemakenerator.core.annotations.Title
import io.github.smiley4.schemakenerator.jackson.jsonschema.handleJacksonJsonSchemaAnnotations
import io.github.smiley4.schemakenerator.jsonschema.OptionalHandling
import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.compileReferencing
import io.github.smiley4.schemakenerator.jsonschema.compileReferencingRoot
import io.github.smiley4.schemakenerator.jsonschema.data.RefType
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.handleCoreAnnotations
import io.github.smiley4.schemakenerator.jsonschema.withTitle
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.kotest.core.spec.style.FreeSpec
import kotlin.reflect.typeOf

class E03_Generating_JsonSchema : FreeSpec({

    "basic schema generation" - {
        // after extracting and modifying data of classes and types, the json-schema is created with the steps:
        // 1. "generateJsonSchema"-step: generates an independent json-schema for each processed type. At this stage, the schemas may not be 100% valid.
        // 2. "compile[...]"-step: this step takes the independent json-schemas created by the generateJsonSchema-step and merges them into one or multiple final valid schemas

        "inline" {

            // the "compileInlining"-step takes all individual schemas and merges them into one single json-schema by inlining all referenced types.

            val jsonSchema = typeOf<ExampleClass>()
                .processReflection()
                .generateJsonSchema()
                .compileInlining()
                .json.prettyPrint()

            println(jsonSchema)
            // {
            //    "type": "object",
            //    "required": [ "nested", "number" ],
            //    "properties": {
            //       "nested": {
            //          "type": "object",
            //          "required": [ "flag" ],
            //          "properties": {
            //             "flag": {
            //                "type": "boolean"
            //             }
            //          }
            //       },
            //       "number": {
            //          "type": "integer",
            //          "minimum": -2147483648,
            //          "maximum": 2147483647
            //       },
            //       "text": {
            //          "type": "string"
            //       }
            //    }
            // }

        }

        "referencing" {

            // the "compileReferencing"-step takes all individual schemas and only inlines all primitive or simple schemas. All other schemas are referenced.

            val jsonSchema = typeOf<ExampleClass>()
                .processReflection()
                .generateJsonSchema()
                .compileReferencing()

            println(jsonSchema.json.prettyPrint())
            // {
            //    "type": "object",
            //    "required": [
            //       "nested",
            //       "number"
            //    ],
            //    "properties": {
            //       "nested": {
            //          "$ref": "#/definitions/io.github.smiley4.schemakenerator.examples.E03_Generating_JsonSchema.Companion.NestedClass"
            //       },
            //       "number": {
            //          "type": "integer",
            //          "minimum": -2147483648,
            //          "maximum": 2147483647
            //       },
            //       "text": {
            //          "type": "string"
            //       }
            //    }
            // }

            println(jsonSchema.definitions["io.github.smiley4.schemakenerator.examples.E03_Generating_JsonSchema.Companion.NestedClass"]!!.prettyPrint())
            // {
            //    "type": "object",
            //    "required": [
            //       "flag"
            //    ],
            //    "properties": {
            //       "flag": {
            //          "type": "boolean"
            //       }
            //    }
            // }

        }

        "referencing (including root schema)" {

            // the "compileReferencingRoot"-step takes all individual schemas and only inlines all primitive or simple schemas. All other schemas including the original root schema are referenced.

            val jsonSchema = typeOf<ExampleClass>()
                .processReflection()
                .generateJsonSchema()
                .compileReferencingRoot()

            println(jsonSchema.json.prettyPrint())
            // {
            //    "$ref": "#/definitions/io.github.smiley4.schemakenerator.examples.E03_Generating_JsonSchema.Companion.ExampleClass"
            // }

            println(jsonSchema.definitions["io.github.smiley4.schemakenerator.examples.E03_Generating_JsonSchema.Companion.ExampleClass"]!!.prettyPrint())
            // {
            //    "type": "object",
            //    "required": [
            //       "nested",
            //       "number"
            //    ],
            //    "properties": {
            //       "nested": {
            //          "$ref": "#/definitions/io.github.smiley4.schemakenerator.examples.E03_Generating_JsonSchema.Companion.NestedClass"
            //       },
            //       "number": {
            //          "type": "integer",
            //          "minimum": -2147483648,
            //          "maximum": 2147483647
            //       },
            //       "text": {
            //          "type": "string"
            //       }
            //    }
            // }

            println(jsonSchema.definitions["io.github.smiley4.schemakenerator.examples.E03_Generating_JsonSchema.Companion.NestedClass"]!!.prettyPrint())
            // {
            //    "type": "object",
            //    "required": [
            //       "flag"
            //    ],
            //    "properties": {
            //       "flag": {
            //          "type": "boolean"
            //       }
            //    }
            // }
        }

    }

    "configuring the generation step" {

        // The behavior and output of the "generateJsonSchema()"-step can be configured:

        typeOf<SimpleClass>()
            .processReflection()
            .generateJsonSchema {
                // configure how optional properties, i.e properties that are not necessarily nullable but have a default value assigned, should be treated
                // "REQUIRED":     these properties are handled as "required"
                // "NON_REQUIRED": these properties are handled as "not required"
                optionalHandling = OptionalHandling.REQUIRED
            }

    }

    "reference path type" - {

        // schemas can be referenced using their simple or full name

        "simple name" {

            val jsonSchema = typeOf<List<GenericClass<String>>>()
                .processReflection()
                .generateJsonSchema()
                .compileReferencing(pathType = RefType.SIMPLE) // "SIMPLE" takes the simple/short name of the type for the reference path

            println(jsonSchema.json.prettyPrint())
            // {
            //    "type": "array",
            //    "items": {
            //       "$ref": "#/definitions/GenericClass<String>"
            //    }
            // }

        }

        "full name" {

            val jsonSchema = typeOf<List<GenericClass<String>>>()
                .processReflection()
                .generateJsonSchema()
                .compileReferencing(pathType = RefType.FULL) // "FULL" takes the full/qualified name of the type for the reference path

            println(jsonSchema.json.prettyPrint())
            // {
            //    "type": "array",
            //    "items": {
            //       "$ref": "#/definitions/io.github.smiley4.schemakenerator.examples.E03_Generating_JsonSchema.Companion.GenericClass<kotlin.String>"
            //    }
            // }
        }

    }

    "automatically adding a schema title" {

        // a "title" property can be automatically added to all types in the json-schema

        val jsonSchema = typeOf<List<GenericClass<String>>>()
            .processReflection()
            .generateJsonSchema()
            .withTitle(type = TitleType.SIMPLE)
            .compileInlining()

        // "TitleType.SIMPLE" takes the simple/short name of the type as the title.
        // "TitleType.FULL" takes the full/qualified name of the type as the title.

        println(jsonSchema.json.prettyPrint())
        // {
        //    "title": "List<GenericClass<String>>",
        //    "type": "array",
        //    "items": {
        //       "title": "GenericClass<String>",
        //       "type": "object",
        //       "required": [ "value" ],
        //       "properties": {
        //          "value": {
        //             "title": "String",
        //             "type": "string",
        //          }
        //       },
        //    },
        // }

    }


    "adding additional information with annotations" - {

        // properties of the json-schema can be filled with additional information from annotations, e.g. descriptions or default values

        "schema-kenerator-core annotations" {

            // the "handleCoreAnnotations()"-step adds information from annotations from schema-kenerator-core to the json-schema

            val jsonSchema = typeOf<CoreAnnotatedClass>()
                .processReflection()
                .generateJsonSchema()
                .handleCoreAnnotations()
                .compileInlining()

            println(jsonSchema.json.prettyPrint())
            // {
            //    "title": "Annotated Class",
            //    "description": "some description",
            //    "type": "object",
            //    "default": "default value",
            //    "deprecated": true,
            //    "examples": [
            //       "example 1"
            //    ],
            //    "required": [
            //       "intValue",
            //       "stringValue"
            //    ],
            //    "properties": {
            //       "intValue": {
            //          "type": "integer",
            //          "description": "Int field description",
            //          "minimum": -2147483648,
            //          "maximum": 2147483647,
            //          "default": "1111",
            //          "examples": [
            //             "2222"
            //          ]
            //       },
            //       "stringValue": {
            //          "type": "string",
            //          "description": "String field description",
            //          "default": "A default String value",
            //          "examples": [
            //             "An example of a String value"
            //          ]
            //       }
            //    },
            // }

        }

        "jackson annotations" {

            // the "handleJacksonJsonSchemaAnnotations()"-step adds information from jackson annotations to the json-schema

            val jsonSchema = typeOf<JacksonAnnotatedClass>()
                .processReflection()
                .generateJsonSchema()
                .handleJacksonJsonSchemaAnnotations()
                .compileInlining()

            println(jsonSchema.json.prettyPrint())
            // {
            //    "type": "object",
            //    "required": [ "value" ],
            //    "properties": {
            //       "value": {
            //          "description": "Example description of the field"
            //          "type": "string",
            //       }
            //    }
            // }
        }

    }

}) {
    companion object {

        class NestedClass(
            val flag: Boolean
        )

        class ExampleClass(
            val nested: NestedClass,
            val number: Int,
            val text: String?,
        )

        class SimpleClass(
            val nested: NestedClass
        )

        class GenericClass<T>(
            val value: T
        )


        @Title("Annotated Class")
        @Description("some description")
        @Default("default value")
        @Example("example 1")
        @Deprecated
        class CoreAnnotatedClass(
            @Description("String field description")
            @Default("A default String value")
            @Example("An example of a String value")
            val stringValue: String,

            @Description("Int field description")
            @Default("1111")
            @Example("2222")
            val intValue: Int,
        )

        class JacksonAnnotatedClass(
            @JsonPropertyDescription("Example description of the field")
            val value: String
        )

    }
}
