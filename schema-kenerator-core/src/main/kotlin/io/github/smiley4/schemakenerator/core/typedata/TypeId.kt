package io.github.smiley4.schemakenerator.core.typedata

import java.util.UUID

/**
 * Unique id for types
 */
data class TypeId(val id: String) {

    companion object {
        val WILDCARD = TypeId("*")
        fun createWildcard() = WILDCARD
        fun create() = TypeId(UUID.randomUUID().toString())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        return id == (other as TypeId).id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}
