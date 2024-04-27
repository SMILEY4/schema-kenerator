package io.github.smiley4.schemakenerator.core.parser

data class TypeId(val id: String) {

    companion object {

        fun wildcard() = TypeId("*")
        fun unknown() = TypeId("?")

        fun build(name: String): TypeId {
            return TypeId(name.replace("?", ""))
        }


        @JvmName("buildTypeIdParams")
        fun build(name: String, typeParameters: List<TypeId>): TypeId {
            return build(name.replace("?", ""), typeParameters.map { it.id })
        }

        @JvmName("buildTypeStringParams")
        fun build(name: String, typeParameters: List<String>): TypeId {
            var id = name.replace("?", "")
            if (typeParameters.isNotEmpty()) {
                id += "<${typeParameters.joinToString(",")}>"
            }
            return TypeId(id)
        }

    }

}