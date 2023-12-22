package io.github.smiley4.schemakenerator.parser.core

data class TypeId(val id: String) {

    companion object {

        fun wildcard() = TypeId("*")

        fun build(name: String): TypeId {
            return TypeId(name)
        }

        @JvmName("buildIdTypeParams")
        fun build(name: String, typeParameters: List<TypeId>): TypeId {
            return build(name, typeParameters.map { it.id })
        }

        @JvmName("buildStringTypeParams")
        fun build(name: String, typeParameters: List<String>): TypeId {
            var id = name
            if (typeParameters.isNotEmpty()) {
                id += "<${typeParameters.joinToString(",")}>"
            }
            return TypeId(id)
        }

    }

}