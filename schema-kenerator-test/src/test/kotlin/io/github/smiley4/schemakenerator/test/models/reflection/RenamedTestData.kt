package io.github.smiley4.schemakenerator.test.models.reflection

import io.github.smiley4.schemakenerator.core.annotations.Name

@Name("TestData", "test.TestData")
data class RenamedTestData(val nestedValue: RenamedNestedData<String>)

@Name("NestedData", "test.NestedData")
data class RenamedNestedData<T>(val someValue: T)