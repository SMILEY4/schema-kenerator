package io.github.smiley4.schemakenerator.test.models.kotlinx

import kotlinx.serialization.Serializable

@Serializable
class ClassWithCollections(
    val someList: List<String>,
    val someSet: Set<String>,
    val someMap: Map<String, Int>,
    val someArray: IntArray,
)