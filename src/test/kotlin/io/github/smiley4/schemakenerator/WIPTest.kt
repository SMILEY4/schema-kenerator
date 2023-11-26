package io.github.smiley4.schemakenerator

import io.kotest.core.spec.style.StringSpec

class WIPTest : StringSpec({

    "SimpleDataClass" {
        val analyzer = ClassAnalyzer()
        val info = analyzer.analyze<SimpleDataClass>()
        println(info)
    }

    "GenericDataClass" {
        val analyzer = ClassAnalyzer()
        val info = analyzer.analyze<GenericDataClass<String, Int>>()
        println(info)
    }

    "NestedDataClass" {
        val analyzer = ClassAnalyzer()
        val info = analyzer.analyze<NestedDataClass>()
        println(info)
    }

}) {
    companion object {

        data class SimpleDataClass(
            val someInt: Int,
            val someString: String,
            val optionalString: String?,
            val someBoolean: Boolean,
            val someAny: Any
        )

        data class GenericDataClass<V,F>(
            val someValue: V,
            val someField: F,
        )

        data class NestedDataClass(
            val someField: String,
            val someObject: SimpleDataClass,
            val simpleList: List<Int>,
            val objectList: List<SimpleDataClass>
        )


    }
}