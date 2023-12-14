package io.github.smiley4.schemakenerator.newdata

import kotlin.reflect.KType


class AnalysisContext(
    private val types: MutableMap<TypeRef, TypeInformation> = mutableMapOf()
) {

    fun getTypeRef(type: KType): TypeRef? {
        return types.keys.find { it.type == type }
    }

    fun addType(type: KType, typeInformation: TypeInformation): TypeRef {
        val ref = TypeRef(type)
        types[ref] = typeInformation
        return ref
    }

    fun getType(ref: TypeRef): TypeInformation? {
        return types[ref]
    }

}