package io.github.smiley4.schemakenerator.core.data

class TypeId(
    val base: String,
    val typeParameters: List<TypeId>,
    val additionalId: String?
) {

    companion object {

        fun wildcard() = TypeId("*", emptyList(), null)

        fun unknown() = TypeId("?", emptyList(), null)

        fun build(name: String) = TypeId(name, emptyList(), null)

        fun build(name: String, typeParameters: List<TypeId>) = TypeId(name, typeParameters, null)

        fun parse(fullTypeId: String): TypeId {

            // base
            val base = fullTypeId
                .let {
                    if (it.contains("<")) {
                        it.split("<").first()
                    } else {
                        it
                    }
                }
                .let {
                    if (it.contains("#")) {
                        it.split("#")
                            .toMutableList().also { parts -> parts.removeLast() }
                            .joinToString("#")

                    } else {
                        it
                    }
                }

            // additional id
            var additionalId: String? = null
            if (fullTypeId.contains("#")) {
                additionalId = fullTypeId.split("#").last()
            }

            var typeParameters = emptyList<TypeId>()
            if (fullTypeId.contains("<")) {
                val paramList = fullTypeId
                    .split("<")
                    .toMutableList().also { it.removeFirst() }
                    .joinToString("<")
                    .let {
                        if (it.contains("#")) {
                            it
                                .split("#")
                                .toMutableList()
                                .also { l -> l.removeLast() }
                                .joinToString("#")
                        } else {
                            it
                        }
                    }
                    .let { it.substring(0, it.length - 1) }

                val paramFullIds = mutableListOf<String>()
                var current = ""
                var nestedLevel = 0
                paramList.toCharArray().forEach { c ->
                    when (c) {
                        ',' -> if (nestedLevel == 0) {
                            paramFullIds.add(current)
                            current = ""
                        } else {
                            current += c
                        }
                        '<' -> {
                            nestedLevel++
                            current += c
                        }
                        '>' -> {
                            nestedLevel--
                            current += c
                        }
                        else -> current += c
                    }
                }
                paramFullIds.add(current)
                typeParameters = paramFullIds.map { parse(it) }
            }


            return TypeId(
                base = base,
                typeParameters = typeParameters,
                additionalId = additionalId
            )
        }

//        fun build(name: String): TypeId {
//            return TypeId(name.replace("?", ""))
//        }
//
//
//        @JvmName("buildTypeIdParams")
//        fun build(name: String, typeParameters: List<TypeId>): TypeId {
//            return build(name.replace("?", ""), typeParameters.map { it.id })
//        }
//
//        @JvmName("buildTypeStringParams")
//        fun build(name: String, typeParameters: List<String>): TypeId {
//            var id = name.replace("?", "")
//            if (typeParameters.isNotEmpty()) {
//                id += "<${typeParameters.joinToString(",")}>"
//            }
//            return TypeId(id)
//        }

    }

    fun full(): String {
        return base
            .let {
                if (typeParameters.isNotEmpty()) {
                    it + "<" + typeParameters.joinToString(",") { p -> p.full() } + ">"
                } else {
                    it
                }
            }
            .let {
                if (additionalId != null) {
                    "$it#$additionalId"
                } else {
                    it
                }
            }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is TypeId) return false
        if (base != other.base) return false
        if (additionalId != other.additionalId) return false
        if (typeParameters.size != other.typeParameters.size) return false
        if (typeParameters.zip(other.typeParameters).any { (a, b) -> a != b }) return false
        return true
    }

    override fun hashCode(): Int {
        var result = base.hashCode()
        result = 31 * result + typeParameters.hashCode()
        result = 31 * result + (additionalId?.hashCode() ?: 0)
        return result
    }

}