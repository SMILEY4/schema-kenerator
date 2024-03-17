package io.github.smiley4.schemakenerator.reflection.models

class TestClassWithMethods(
    val someText: String,
    val myFlag: Boolean,
    val isEnabled: Boolean,
    private val hiddenField: String = "hidden"
) {
    fun calculateValue(): Int = 4
    fun compare(other: Any): Long = 3L
    fun myFlag() = myFlag.toString()
    fun isDisabled() = !isEnabled
    private fun hiddenFunction(): String = "hidden"
}