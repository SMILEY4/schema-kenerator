package io.github.smiley4.schemakenerator.analysis

import io.github.smiley4.schemakenerator.analysis.data.TypeData
import io.github.smiley4.schemakenerator.analysis.data.TypeParameterData
import io.github.smiley4.schemakenerator.analysis.data.TypeRef
import kotlin.reflect.KType


class TypeContext {

    private val types = mutableMapOf<String, TypeData>()

    fun add(kType: KType, type: TypeData): TypeRef {
        val ref = TypeRef.forType(kType, type.typeParameters)
        types[ref.id] = type
        return ref
    }

    fun add(ref: TypeRef, type: TypeData): TypeRef {
        types[ref.id] = type
        return ref
    }

    fun reserve(ref: TypeRef) {
        add(ref, TypeData.placeholder(ref))
    }

    fun getData(kType: KType, typeParameters: Map<String, TypeParameterData>): TypeData? {
        return getData(TypeRef.forType(kType, typeParameters))
    }

    fun getData(ref: TypeRef): TypeData? {
        return types[ref.id]
    }

    fun has(kType: KType, typeParameters: Map<String, TypeParameterData>): Boolean {
        return has(TypeRef.forType(kType, typeParameters))
    }

    fun has(ref: TypeRef): Boolean {
        return types.containsKey(ref.id)
    }

    fun getIds(): List<String> {
        return types.keys.toList()
    }

}
