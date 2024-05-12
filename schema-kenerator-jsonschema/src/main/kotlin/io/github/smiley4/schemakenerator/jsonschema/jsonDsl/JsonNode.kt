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
}


/**
 * An object with properties
 */
data class JsonObject(val properties: MutableMap<String, JsonNode>) : JsonNode {

    override fun prettyPrint(level: Int): String {
        if(properties.isEmpty()) {
            return "{}"
        }
        return buildString {
            appendLine("{")
            properties.entries.forEachIndexed { index, (key, value) ->
                val isLast = index == properties.size-1
                appendLine(indent(level + 1) + "\"$key\": ${value.prettyPrint(level + 1)}" + if (isLast) "" else ",")
            }
            append(indent(level) + "}")
        }
    }

}


/**
 * An array with items
 */
data class JsonArray(val items: MutableList<JsonNode> = mutableListOf()) : JsonNode {

    override fun prettyPrint(level: Int): String {
        if(items.isEmpty()) {
            return "[]"
        }
        return buildString {
            appendLine("[")
            items.forEachIndexed { index, item ->
                val isLast = index  == items.size-1
                appendLine(indent(level + 1) + item.prettyPrint(level + 1) + if (isLast) "" else ",")
            }
            append(indent(level) + "]")
        }
    }

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
}

/**
 * A node with a text value
 */
class JsonTextValue(value: String) : JsonValue<String>(value) {
    override fun prettyPrint(level: Int): String {
        return "\"$value\""
    }
}

/**
 * A node with a boolean value
 */
class JsonBooleanValue(value: Boolean) : JsonValue<Boolean>(value) {
    override fun prettyPrint(level: Int): String {
        return "$value"
    }
}

/**
 * A node without a value (i.e. 'null')
 */
class JsonNullValue : JsonValue<Unit>(Unit) {
    override fun prettyPrint(level: Int): String {
        return "null"
    }
}
