package io.github.smiley4.schemakenerator.test.models.reflection

import io.github.smiley4.schemakenerator.core.annotations.SchemaName
import io.github.smiley4.schemakenerator.test.MyNestedDataDto

@SchemaName("TestData", "test.TestData")
data class RenamedTestData(val nestedValue: MyNestedDataDto<String>)

@SchemaName("NestedData", "test.NestedData")
data class RenamedNestedData<T>(val someValue: T)