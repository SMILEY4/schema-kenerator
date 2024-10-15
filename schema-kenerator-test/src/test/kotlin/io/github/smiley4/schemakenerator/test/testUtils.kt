package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.kotest.assertions.json.ArrayOrder
import io.kotest.assertions.json.FieldComparison
import io.kotest.assertions.json.NumberFormat
import io.kotest.assertions.json.PropertyOrder
import io.kotest.assertions.json.TypeCoercion
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.swagger.v3.core.util.Json31
import io.swagger.v3.oas.models.media.Schema

fun Schema<*>.shouldEqualJson(expected: () -> String) = this.shouldEqualJson(expected())

fun Schema<*>.shouldEqualJson(expected: String) {
    Json31.pretty(this).shouldEqualLenient(expected)
}

fun JsonNode.shouldEqualJson(expected: () -> String) = this.shouldEqualJson(expected())

fun JsonNode.shouldEqualJson(expected: String) {
    this.prettyPrint().shouldEqualLenient(expected)
}

fun Pair<Schema<*>, Map<String,Schema<*>>>.shouldEqualJson(expected: () -> Map<String,String>) = this.shouldEqualJson(expected())

fun Pair<Schema<*>, Map<String,Schema<*>>>.shouldEqualJson(expected: Map<String,String>) {
    Json31.pretty(this.first).shouldEqualLenient(expected["."]!!)
    this.second.keys shouldContainExactlyInAnyOrder expected.keys.minus(".")
    this.second.forEach { (key, value) ->
        Json31.pretty(value).shouldEqualLenient(expected[key]!!)
    }
}

fun String.shouldEqualLenient(expected: String): String {
    return this.shouldEqualJson {
        propertyOrder = PropertyOrder.Lenient
        arrayOrder = ArrayOrder.Lenient
        fieldComparison = FieldComparison.Strict
        numberFormat = NumberFormat.Lenient
        typeCoercion = TypeCoercion.Disabled
        expected
    }
}