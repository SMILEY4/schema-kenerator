package io.github.smiley4.schemakenerator.core.schema

import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.github.smiley4.schemakenerator.core.parser.TypeRef

interface SchemaGenerator<T> {

    fun generate(typeRef: TypeRef, context: TypeParserContext): T

}