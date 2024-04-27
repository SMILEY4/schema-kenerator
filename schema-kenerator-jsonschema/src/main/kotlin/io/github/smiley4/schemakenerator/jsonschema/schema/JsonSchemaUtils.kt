package io.github.smiley4.schemakenerator.jsonschema.schema

import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.jsonschema.json.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.json.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.json.array
import io.github.smiley4.schemakenerator.jsonschema.json.obj

class JsonSchemaUtils {

    //=====  BASIC TYPES ============================
    // https://cswr.github.io/JsonSchema/spec/basic_types/

    /**
     * Schema of a text
     * @param min the min length of the string (inclusive)
     * @param max the max length of the string (inclusive)
     */
    fun stringSchema(min: Int?, max: Int?): JsonObject {
        return obj {
            "type" to "string"
            min?.also { "minLength" to it }
            max?.also { "maxLength" to it }
            //"pattern" to "^[A-Za-z]*@gmail.com$"
        }
    }


    /**
     * Schema of a number
     * @param integer whether the number is an integer or a floating-point value
     * @param min the min value of the number (inclusive)
     * @param max the max value of the number (inclusive)
     */
    fun numericSchema(integer: Boolean, min: Number?, max: Number?): JsonObject {
        return obj {
            "type" to if (integer) "integer" else "number"
            min?.also { "minimum" to it }
            max?.also { "maximum" to it }
            //"exclusiveMaximum" to true // inclusive by default
            //"exclusiveMinimum" to true // inclusive by default
            //"multipleOf" to 10
        }
    }


    /**
     * Schema of a boolean
     */
    fun booleanSchema(): JsonObject {
        return obj {
            "type" to "boolean"
        }
    }


    /**
     * Schema for 'null'
     */
    fun nullSchema(): JsonObject {
        return obj {
            "type" to "null"
        }
    }

    //=====  ARRAYS =================================
    // https://cswr.github.io/JsonSchema/spec/arrays/

    fun arraySchema(items: JsonNode): JsonObject {
        return obj {
            "type" to "array"
            "items" to items
            //"uniqueItems" to false
            //"maxItems" to 10
            //"minItems" to 0
            //"maxItems" to 99

        }
    }

    //=====  OBJECTS ================================

    /**
     * Schema for a json object with the given properties
     * @param properties the allowed properties of the object. Key is the name of the property.
     * @param required the names of the required (non-nullable) properties
     */
    fun objectSchema(properties: Map<String, JsonNode>, required: Collection<String>): JsonObject {
        return obj {
            "type" to "object"
            "required" to required
            "properties" to obj {
                properties.forEach { (key, value) ->
                    key to value
                }
            }
        }
    }


    /**
     * Schema for a key-value-map object with the given schema for the values
     * @param valueSchema the schema for the values
     */
    fun mapObjectSchema(valueSchema: JsonNode): JsonObject {
        return obj {
            "type" to "object"
            "additionalProperties" to valueSchema
        }
    }


    /**
     * Schema for any json object
     */
    fun anyObjectSchema(): JsonObject {
        return obj {
            "type" to "object"
        }
    }

    //=====  OBJECTS ================================

    fun subtypesSchema(subtypes: List<JsonNode>): JsonObject {
        return obj {
            "anyOf" to array {
                subtypes.forEach { subtype ->
                    item(subtype)
                }
            }
        }
    }

    //=====  ENUM ===================================

    fun enumSchema(values: Collection<String>): JsonObject {
        return obj {
            "enum" to array {
                values.toSet().forEach { value ->
                    item(value)
                }
            }
        }
    }

    //=====  REFERENCE ==============================

    fun referenceSchema(id: TypeId, isInDefinitions: Boolean = false): JsonObject {
        return referenceSchema(id.id, isInDefinitions)
    }

    fun referenceSchema(id: String, isInDefinitions: Boolean = false): JsonObject {
        return obj {
            "\$ref" to if(isInDefinitions) "#/definitions/$id" else id
        }
    }

}
