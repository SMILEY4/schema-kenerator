package io.github.smiley4.schemakenerator.core.data

import kotlin.math.abs
import kotlin.random.Random

/**
 * Identifier of a type
 */
class TypeId(
    /**
     * the base name (e.g. the qualified class-name
     */
    val base: String,
    /**
     * ids of type parameters, i.e. generics (order is relevant)
     */
    val typeParameters: List<TypeId>,
    /**
     * optional (random) additional id to avoid collisions
     */
    val additionalId: String?
) {

    companion object {

        /**
         * @return id for a wildcard type
         */
        fun wildcard() = TypeId("*", emptyList(), null)


        /**
         * @param name the (qualified) name of the type
         * @return id with the given name
         */
        fun build(name: String) = TypeId(name, emptyList(), null)


        /**
         * @param name the (qualified) name of the type
         * @param typeParameters the list of ids of the type parameters
         * @param withAdditionalId whether set the [TypeId.additionalId] to a random value
         * @return id with the given name and type parameters
         */
        fun build(name: String, typeParameters: List<TypeId>, withAdditionalId: Boolean = false) =
            TypeId(name, typeParameters, if (withAdditionalId) abs(Random.nextLong()).toString() else null)


        /**
         * @param fullTypeId id of a type as a formatted string, i.e. the output of [TypeId.full]
         * @return a id with the data from the given string
         */
        fun parse(fullTypeId: String): TypeId {
            return TypeId(
                base = parseBase(fullTypeId),
                typeParameters = parseTypeParameters(fullTypeId),
                additionalId = parseAdditionalId(fullTypeId)
            )
        }

        private fun parseBase(fullTypeId: String): String {
            return fullTypeId
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
        }

        private fun parseTypeParameters(fullTypeId: String): List<TypeId> {
            if (!fullTypeId.contains("<") || !fullTypeId.contains(">")) {
                return emptyList()
            }
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
            return paramFullIds.map { parse(it) }
        }

        private fun parseAdditionalId(fullTypeId: String): String? {
            return if (fullTypeId.contains("#")) {
                fullTypeId.split("#").last()
            } else {
                null
            }
        }

    }


    /**
     * @return this id as a formatted string containing all information
     */
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


    /**
     * @return this id as a simpler, shorter formatted string
     */
    fun simple(): String {
        var title = base.split(".").last()
        if (typeParameters.isNotEmpty()) {
            title = title + "<" + typeParameters.joinToString(",") { it.simple() } + ">"
        }
        if (additionalId != null) {
            title = "$title#$additionalId"
        }
        return title
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
