package io.github.smiley4.schemakenerator.core.parser

class TypeParserContext {

    private val types = mutableMapOf<String, BaseTypeData>()

    fun add(type: BaseTypeData): TypeId {
        types[type.id.id] = type
        return type.id
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

}
