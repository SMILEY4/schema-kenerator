package io.github.smiley4.schemakenerator.test.models.kotlinx

import kotlinx.serialization.Serializable

interface NullableClasses {
    @Serializable
    class NullableFirst(val t1: Test?, val t2: Test)

    @Serializable
    class NullableSecond(val t1: Test, val t2: Test?)

    @Serializable
    class Test(val count: Int)
}