package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup
import io.github.smiley4.schemakenerator.core.data.TypeId

internal class AddMissingSubtypeSupertypeRelations {

    fun process(input: TypeDataGroup): TypeDataGroup {
        input.typeData.forEach {
            addMissing(it, input.data)
        }
        return input
    }


    /**
     * Add the given type as supertype/subtype to other types in the given list
     */
    private fun addMissing(type: TypeData, dataList: Map<TypeId, TypeData>) {
        // check if this type is missing as the supertype in its subtypes
        type.subtypes.forEach { subtypeId ->
            val subtype = dataList[subtypeId]!!
            if (subtype.supertypes.none { it == type.id }) {
                subtype.supertypes.add(type.id)
            }
        }
        // check if this type is missing as a subtype in its supertype
        type.supertypes.forEach { supertypeId ->
            val supertype = dataList[supertypeId]!!
            if (supertype.subtypes.none { it == type.id }) {
                supertype.subtypes.add(type.id)
            }
        }
    }

}
