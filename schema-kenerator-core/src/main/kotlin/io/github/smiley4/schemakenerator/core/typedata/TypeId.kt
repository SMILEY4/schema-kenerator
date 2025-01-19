package io.github.smiley4.schemakenerator.core.typedata

import java.util.UUID

/**
 * Unique id for types
 */
data class TypeId(val id: String) {
    companion object {
        fun create() = TypeId(UUID.randomUUID().toString())
    }
}