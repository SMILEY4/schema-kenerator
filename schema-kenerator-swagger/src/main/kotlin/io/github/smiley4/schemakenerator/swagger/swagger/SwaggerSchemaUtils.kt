package io.github.smiley4.schemakenerator.swagger.swagger

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
    fun stringSchema(min: Int?, max: Int?): Schema<*> {
        return Schema<Any>().also { schema ->
            schema.type = "string"
            min?.also { schema.minimum = BigDecimal(it) }
            max?.also { schema.maximum = BigDecimal(it) }
        }
    }

    /**
     * Schema of a number
     * @param integer whether the number is an integer or a floating-point value
     * @param min the min value of the number (inclusive)
     * @param max the max value of the number (inclusive)
     */
    fun numericSchema(integer: Boolean, min: Number?, max: Number?): Schema<*> {
        return Schema<Any>().also { schema ->
            schema.type = if(integer) "integer" else "number"
            min?.also { schema.minimum = if(it is Float || it is Double) BigDecimal(it.toDouble()) else BigDecimal(it.toLong()) }
            max?.also { schema.maximum = if(it is Float || it is Double) BigDecimal(it.toDouble()) else BigDecimal(it.toLong()) }
        }
    }


    /**
     * Schema of a boolean
     */
    fun booleanSchema(): Schema<*> {
        return Schema<Any>().also { schema ->
            schema.type = "boolean"
        }
    }


    /**
     * Schema for 'null'
     */
    fun nullSchema(): Schema<*> {
        return Schema<Any>().also { schema ->
            schema.type = "null"
        }
    }

    //=====  ARRAYS =================================
    // https://cswr.github.io/JsonSchema/spec/arrays/

    fun arraySchema(items: Schema<*>): Schema<*> {
        return Schema<Any>().also { schema ->
            schema.type = "array"
            schema.items = items
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
            schema.type = "object"
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
            schema.type = "object"
            schema.additionalProperties = valueSchema
        }
    }

    /**
     * Schema for any json object
     */
    fun anyObjectSchema(): Schema<*> {
        return Schema<Any>().also { schema ->
            schema.type = "object"
        }
    }

    //=====  OBJECTS ================================

    fun subtypesSchema(subtypes: List<Schema<*>>): Schema<*> {
        return Schema<Any>().also { schema ->
            schema.anyOf = subtypes
        }
    }


    //=====  ENUM ===================================

    fun enumSchema(values: Collection<String>): Schema<*> {
        return Schema<String>().also { schema ->
            schema.enum = values.toMutableList()
        }
    }

    //=====  REFERENCE ==============================

    fun referenceSchema(id: String): Schema<*> {
        return Schema<String>().also { schema ->
            schema.`$ref` = "#/definitions/$id"
        }
    }

}