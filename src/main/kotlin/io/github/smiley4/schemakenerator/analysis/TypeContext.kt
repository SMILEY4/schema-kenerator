package io.github.smiley4.schemakenerator.analysis

import kotlin.reflect.KType


class TypeContext {

    private val types = mutableMapOf<String, TypeData>()

    fun add(kType: KType, type: TypeData): TypeRef {
        val ref = TypeRef.forType(kType, type.typeParameters)
        types[ref.id] = type
        return ref
    }

    fun getData(kType: KType, typeParameters: Map<String, TypeRef>): TypeData? {
        return getData(TypeRef.forType(kType, typeParameters))
    }

    fun getData(ref: TypeRef): TypeData? {
        return types[ref.id]
    }

    fun has(kType: KType, typeParameters: Map<String, TypeRef>): Boolean {
        return has(TypeRef.forType(kType, typeParameters))
    }

    fun has(ref: TypeRef): Boolean {
        return types.containsKey(ref.id)
    }

}