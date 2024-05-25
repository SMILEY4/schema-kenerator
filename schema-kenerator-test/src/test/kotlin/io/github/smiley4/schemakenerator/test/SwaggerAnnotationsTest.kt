package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleSwaggerArraySchemaAnnotation
import io.github.smiley4.schemakenerator.swagger.handleSwaggerSchemaAnnotation
import io.kotest.assertions.json.ArrayOrder
import io.kotest.assertions.json.FieldComparison
import io.kotest.assertions.json.NumberFormat
import io.kotest.assertions.json.PropertyOrder
import io.kotest.assertions.json.TypeCoercion
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.StringSpec
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import kotlin.reflect.typeOf

class SwaggerAnnotationsTest : StringSpec({

    "swagger annotations" {
        val result = typeOf<MyTestClass>()
            .processReflection()
            .generateSwaggerSchema()
            .handleSwaggerSchemaAnnotation()
            .handleSwaggerArraySchemaAnnotation()
            .compileInlining()
        jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()
            .writeValueAsString(result.swagger).shouldEqualJson {
            propertyOrder = PropertyOrder.Lenient
            arrayOrder = ArrayOrder.Lenient
            fieldComparison = FieldComparison.Strict
            numberFormat = NumberFormat.Lenient
            typeCoercion = TypeCoercion.Disabled
            """
                {
                  "title": "My Test Class",
                  "required": [
                    "myValue",
                    "someTags"
                  ],
                  "type": "object",
                  "properties": {
                    "myValue": {
                      "title": "Some Value",
                      "maximum": 9,
                      "exclusiveMaximum": true,
                      "minimum": 0,
                      "exclusiveMinimum": false,
                      "maxLength": 10,
                      "minLength": 1,
                      "type": "integer",
                      "description": "some value",
                      "format": "single-digit",
                      "readOnly": true,
                      "exampleSetFlag": false,
                      "enum": ["1","2","3","4"],
                      "default": "1"
                    },
                    "someTags": {
                      "maxItems": 10,
                      "minItems": 0,
                      "uniqueItems": true,
                      "type": "array",
                      "exampleSetFlag": false,
                      "items": {
                        "type": "string",
                        "exampleSetFlag": false
                      }
                    }
                  },
                  "description": "some description",
                  "exampleSetFlag": false
                }
            """.trimIndent()
        }
    }

}) {

    companion object {

        @Schema(
            description = "some description",
            title = "My Test Class",
            name = "TestClass",
        )
        private class MyTestClass(

            @field:Schema(
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

            @field:Schema(
                hidden = true,
                name = "hidden-value"
            )
            val hiddenValue: String,

            @field:ArraySchema(
                minItems = 0,
                maxItems = 10,
                uniqueItems = true
            )
            val someTags: List<String>
        )

    }

}