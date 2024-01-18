package io.github.smiley4.schemakenerator.core.parser

sealed interface TypeRef

fun TypeRef.id(): String = when (this) {
    is ContextTypeRef -> this.id.id
    is InlineTypeRef -> this.type.id.id
}

fun TypeRef.resolve(context: TypeParserContext): BaseTypeData? {
    return when (this) {
        is ContextTypeRef -> context.getData(this.id)
        is InlineTypeRef -> this.type
    }
}

class InlineTypeRef(val type: BaseTypeData) : TypeRef

class ContextTypeRef(val id: TypeId) : TypeRef