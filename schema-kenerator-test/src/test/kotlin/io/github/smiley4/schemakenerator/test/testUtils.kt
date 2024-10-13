package io.github.smiley4.schemakenerator.test

import io.kotest.assertions.json.ArrayOrder
import io.kotest.assertions.json.FieldComparison
import io.kotest.assertions.json.NumberFormat
import io.kotest.assertions.json.PropertyOrder
import io.kotest.assertions.json.TypeCoercion
import io.kotest.assertions.json.shouldEqualJson

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