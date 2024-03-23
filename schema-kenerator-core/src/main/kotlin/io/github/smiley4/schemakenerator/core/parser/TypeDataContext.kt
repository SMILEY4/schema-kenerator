package io.github.smiley4.schemakenerator.core.parser

class TypeDataContext {

    private val types = mutableMapOf<String, BaseTypeData>()

    fun add(type: BaseTypeData): TypeRef {
        types[type.id.id] = type
        return ContextTypeRef(type.id)
    }

    fun reserve(id: TypeId): TypeId {
        types[id.id] = BaseTypeData.placeholder(id)
        return id
    }

    fun getData(id: TypeId): BaseTypeData? {
        return types[id.id]
    }

    fun has(id: TypeId): Boolean {
        return types.containsKey(id.id)
    }

    fun getIds(): List<String> {
        return types.keys.toList()
    }

    fun getTypes(): List<BaseTypeData> {
        return types.values.toList()
    }

    fun clear() {
        types.clear()
    }

}
