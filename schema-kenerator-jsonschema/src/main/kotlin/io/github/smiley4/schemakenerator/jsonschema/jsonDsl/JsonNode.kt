package io.github.smiley4.schemakenerator.jsonschema.jsonDsl

/**
 * A generic node
 */
sealed interface JsonNode {

    companion object {
        var indentSize: Int = 3
    }

    fun prettyPrint(level: Int = 0): String

    fun indent(level: Int): String = " ".repeat(level * indentSize)

    fun copyNode(): JsonNode

}


/**
 * An object with properties
 */
data class JsonObject(val properties: MutableMap<String, JsonNode>) : JsonNode {

    fun getText(propertyName: String): String {
        val prop = properties[propertyName]
        if (prop is JsonTextValue) {
            return prop.value
        } else {
            throw NoSuchElementException("Property '$propertyName' is not a ${JsonTextValue::class.simpleName}")
        }
    }

    fun getNumber(propertyName: String): Number {
        val prop = properties[propertyName]
        if (prop is JsonNumericValue) {
            return prop.value
        } else {
            throw NoSuchElementException("Property '$propertyName' is not a ${JsonNumericValue::class.simpleName}")
        }
    }

    fun getBool(propertyName: String): Boolean {
        val prop = properties[propertyName]
        if (prop is JsonBooleanValue) {
            return prop.value
        } else {
            throw NoSuchElementException("Property '$propertyName' is not a ${JsonBooleanValue::class.simpleName}")
        }
    }

    fun getArray(propertyName: String): JsonArray {
        val prop = properties[propertyName]
        if (prop is JsonArray) {
            return prop
        } else {
            throw NoSuchElementException("Property '$propertyName' is not a ${JsonArray::class.simpleName}")
        }
    }

    fun getObject(propertyName: String): JsonObject {
        val prop = properties[propertyName]
        if (prop is JsonObject) {
            return prop
        } else {
            throw NoSuchElementException("Property '$propertyName' is not a ${JsonObject::class.simpleName}")
        }
    }

    override fun prettyPrint(level: Int): String {
        if (properties.isEmpty()) {
            return "{}"
        }
        return buildString {
            appendLine("{")
            properties.entries.forEachIndexed { index, (key, value) ->
                val isLast = index == properties.size - 1
                appendLine(indent(level + 1) + "\"$key\": ${value.prettyPrint(level + 1)}" + if (isLast) "" else ",")
            }
            append(indent(level) + "}")
        }
    }

    override fun copyNode(): JsonNode {
        return JsonObject(
            properties = properties.mapValues { (_, value) -> value.copyNode() }.toMutableMap()
        )
    }

    override fun equals(other: Any?) = other is JsonObject && other.properties == properties

    override fun hashCode() = properties.hashCode()

}


/**
 * An array with items
 */
data class JsonArray(val items: MutableList<JsonNode> = mutableListOf()) : JsonNode {

    override fun prettyPrint(level: Int): String {
        if (items.isEmpty()) {
            return "[]"
        }
        return buildString {
            appendLine("[")
            items.forEachIndexed { index, item ->
                val isLast = index == items.size - 1
                appendLine(indent(level + 1) + item.prettyPrint(level + 1) + if (isLast) "" else ",")
            }
            append(indent(level) + "]")
        }
    }

    override fun copyNode(): JsonNode {
        return JsonArray(
            items = items.map { it.copyNode() }.toMutableList()
        )
    }

    override fun equals(other: Any?) = other is JsonArray && other.items == items

    override fun hashCode() = items.hashCode()
}


/**
 * A node with a value of any type
 */
sealed class JsonValue<T>(val value: T) : JsonNode


/**
 * A node with a numeric value
 */
class JsonNumericValue(value: Number) : JsonValue<Number>(value) {
    override fun prettyPrint(level: Int): String {
        return "$value"
    }

    override fun copyNode() = JsonNumericValue(value)

    override fun equals(other: Any?) = other is JsonNumericValue && other.value == value

    override fun hashCode() = value.hashCode()
}


/**
 * A node with a text value
 */
class JsonTextValue(value: String) : JsonValue<String>(value) {
    override fun prettyPrint(level: Int): String {
        return "\"$value\""
    }

    override fun copyNode() = JsonTextValue(value)

    override fun equals(other: Any?) = other is JsonTextValue && other.value == value

    override fun hashCode() = value.hashCode()
}


/**
 * A node with a boolean value
 */
class JsonBooleanValue(value: Boolean) : JsonValue<Boolean>(value) {
    override fun prettyPrint(level: Int): String {
        return "$value"
    }

    override fun copyNode() = JsonBooleanValue(value)

    override fun equals(other: Any?) = other is JsonBooleanValue && other.value == value

    override fun hashCode() = value.hashCode()
}


/**
 * A node without a value (i.e. 'null')
 */
class JsonNullValue : JsonValue<Unit>(Unit) {
    override fun prettyPrint(level: Int): String {
        return "null"
    }

    override fun copyNode() = JsonNullValue()

    override fun equals(other: Any?) = other is JsonNullValue

    override fun hashCode() = value.hashCode()
}
