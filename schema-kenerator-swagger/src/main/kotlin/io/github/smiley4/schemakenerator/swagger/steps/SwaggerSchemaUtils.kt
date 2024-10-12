package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.TypeId
import io.swagger.v3.oas.models.media.Discriminator
import io.swagger.v3.oas.models.media.Schema
import java.math.BigDecimal

class SwaggerSchemaUtils {

    //=====  BASIC TYPES ============================
    // https://cswr.github.io/JsonSchema/spec/basic_types/

    /**
     * Schema of a text
     * @param min the min length of the string (inclusive)
     * @param max the max length of the string (inclusive)
     */
    fun stringSchema(min: Int?, max: Int?) = Schema<Any>().also { schema ->
        schema.types = setOf("string")
        min?.also { schema.minimum = BigDecimal(it) }
        max?.also { schema.maximum = BigDecimal(it) }
    }


    /**
     * Schema of a number
     * @param integer whether the number is an integer or a floating-point value
     * @param min the min value of the number (inclusive)
     * @param max the max value of the number (inclusive)
     */
    fun numericSchema(integer: Boolean, min: BigDecimal, max: BigDecimal) = Schema<Any>().also { schema ->
        schema.types = setOf(if (integer) "integer" else "number")
        schema.minimum = min
        schema.maximum = max
    }


    /**
     * Schema of any number
     * @param integer whether the number is an integer or a floating-point value
     */
    fun numberSchema(integer: Boolean) = Schema<Any>().also { schema ->
        schema.types = setOf(if (integer) "integer" else "number")
    }


    /**
     * Schema of a floating-point number
     */
    fun floatSchema() = Schema<Any>().also { schema ->
        schema.types = setOf("number")
        schema.format = "float"
    }


    /**
     * Schema of a floating-point number with double precision
     */
    fun doubleSchema() = Schema<Any>().also { schema ->
        schema.types = setOf("number")
        schema.format = "double"
    }


    /**
     * Schema of a signed 32-bit integer
     */
    fun int32Schema() = Schema<Any>().also { schema ->
        schema.types = setOf("integer")
        schema.format = "int32"
    }


    /**
     * Schema of a signed 64-bit integer
     */
    fun int64Schema() = Schema<Any>().also { schema ->
        schema.types = setOf("integer")
        schema.format = "int64"
    }


    /**
     * Schema of a boolean
     */
    fun booleanSchema(): Schema<*> {
        return Schema<Any>().also { schema ->
            schema.types = setOf("boolean")
        }
    }


    /**
     * Schema for 'null'
     */
    fun nullSchema(): Schema<*> {
        return Schema<Any>().also { schema ->
            schema.types = setOf("null")
        }
    }

    //=====  ARRAYS =================================
    // https://cswr.github.io/JsonSchema/spec/arrays/

    fun arraySchema(items: Schema<*>, uniqueItems: Boolean = false): Schema<*> {
        return Schema<Any>().also { schema ->
            schema.types = setOf("array")
            schema.items = items
            if (uniqueItems) {
                schema.uniqueItems = true
            }
        }
    }

    //=====  OBJECTS ================================

    /**
     * Schema for a json object with the given properties
     * @param properties the allowed properties of the object. Key is the name of the property.
     * @param required the names of the required (non-nullable) properties
     */
    fun objectSchema(properties: Map<String, Schema<*>>, required: Collection<String>): Schema<*> {
        return Schema<Any>().also { schema ->
            schema.types = setOf("object")
            schema.required = required.toMutableList()
            schema.properties = properties
        }
    }


    /**
     * Schema for a key-value-map object with the given schema for the values
     * @param valueSchema the schema for the values
     */
    fun mapObjectSchema(valueSchema: Schema<*>): Schema<*> {
        return Schema<Any>().also { schema ->
            schema.types = setOf("object")
            schema.additionalProperties = valueSchema
        }
    }


    /**
     * Schema for any json object
     */
    fun anyObjectSchema(): Schema<*> {
        return Schema<Any>().also { schema ->
            schema.types = setOf("object")
        }
    }

    //=====  OBJECTS ================================

    fun subtypesSchema(subtypes: List<Schema<*>>, discriminator: String? = null): Schema<*> {
        return Schema<Any>().also { schema ->
            schema.anyOf = subtypes
            if (discriminator != null) {
                schema.discriminator = Discriminator().also {
                    it.propertyName = discriminator
                }
            }
        }
    }

    //=====  ENUM ===================================

    fun enumSchema(values: Collection<String>): Schema<*> {
        return Schema<String>().also { schema ->
            schema.enum = values.toMutableList()
        }
    }

    //=====  REFERENCE ==============================

    fun referenceSchema(id: TypeId, isInComponents: Boolean = false): Schema<*> {
        return referenceSchema(id.full(), isInComponents)
    }

    fun referenceSchema(id: String, isInComponents: Boolean = false): Schema<*> {
        return Schema<String>().also { schema ->
            schema.`raw$ref`(if (isInComponents) "#/components/schemas/$id" else id)
        }
    }

    fun referenceSchemaNullable(id: String, isInComponents: Boolean = false): Schema<*> {
        return Schema<String>().also { schema ->
            schema.oneOf = buildList {
                add(nullSchema())
                add(referenceSchema(id, isInComponents))
            }
        }
    }

}
