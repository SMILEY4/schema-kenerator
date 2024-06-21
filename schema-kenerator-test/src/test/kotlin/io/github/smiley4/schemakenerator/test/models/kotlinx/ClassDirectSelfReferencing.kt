package io.github.smiley4.schemakenerator.test.models.kotlinx

import kotlinx.serialization.Serializable

@Serializable
class ClassDirectSelfReferencing(
    val self: ClassDirectSelfReferencing?
)