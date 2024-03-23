package io.github.smiley4.schemakenerator.jsonschema.json

fun obj(block: JsonObjectDsl.() -> Unit): JsonObject {
    return JsonObjectDsl().apply(block).build()
}

class JsonObjectDsl {

    private val properties = mutableMapOf<String, JsonNode>()

    fun build(): JsonObject {
        return JsonObject(properties)
    }

    fun hasProperty(key: String) = properties.containsKey(key)

    infix fun String.to(json: JsonNode?) {
        properties[this] = json ?: JsonNullValue()
    }

    infix fun String.to(value: Number?) {
        properties[this] = value?.let { JsonNumericValue(it) } ?: JsonNullValue()
    }

    infix fun String.to(value: String?) {
        properties[this] = value?.let { JsonTextValue(it) } ?: JsonNullValue()
    }

    infix fun String.to(value: Boolean?) {
        properties[this] = value?.let { JsonBooleanValue(it) } ?: JsonNullValue()
    }

    @JvmName("toJson")
    infix fun String.to(value: Collection<JsonNode?>?) {
        properties[this] = value?.let {
            JsonArray(
                value
                    .map { it ?: JsonNullValue() }
                    .toMutableList()
            )
        } ?: JsonNullValue()
    }

    @JvmName("toString")
    infix fun String.to(value: Collection<String?>?) {
        properties[this] = value?.let {
            JsonArray(
                value
                    .map { it?.let { JsonTextValue(it) } ?: JsonNullValue() }
                    .toMutableList()
            )
        } ?: JsonNullValue()
    }

    @JvmName("toNumber")
    infix fun String.to(value: Collection<Number?>?) {
        properties[this] = value?.let {
            JsonArray(
                value
                    .map { it?.let { JsonNumericValue(it) } ?: JsonNullValue() }
                    .toMutableList()
            )
        } ?: JsonNullValue()
    }

    @JvmName("toBoolean")
    infix fun String.to(value: Collection<Boolean?>?) {
        properties[this] = value?.let {
            JsonArray(
                value
                    .map { it?.let { JsonBooleanValue(it) } ?: JsonNullValue() }
                    .toMutableList()
            )
        } ?: JsonNullValue()
    }

    infix fun String.to(value: Nothing?) {
        properties[this] = JsonNullValue()
    }

}

fun array(block: JsonArrayDsl.() -> Unit): JsonArray {
    return JsonArrayDsl().apply(block).build()
}

class JsonArrayDsl {

    private val items = mutableListOf<JsonNode>()

    fun build(): JsonArray {
        return JsonArray(items)
    }

    fun item(json: JsonNode?) {
        items.add(json ?: JsonNullValue())
    }

    fun item(value: Number?) {
        items.add(value?.let { JsonNumericValue(it) } ?: JsonNullValue())
    }

    fun item(value: String?) {
        items.add(value?.let { JsonTextValue(it) } ?: JsonNullValue())
    }

    fun item(value: Boolean?) {
        items.add(value?.let { JsonBooleanValue(it) } ?: JsonNullValue())
    }

    fun item(value: Nothing?) {
        items.add(JsonNullValue())
    }

}