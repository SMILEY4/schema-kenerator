package io.github.smiley4.schemakenerator.core.schema

import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.core.parser.TypeRef

interface SchemaGenerator<T> {

    fun generate(typeRef: TypeRef, context: TypeDataContext, depth: Int = 0): T

}