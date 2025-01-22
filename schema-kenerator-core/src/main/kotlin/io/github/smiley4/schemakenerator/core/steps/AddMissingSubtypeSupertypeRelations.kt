package io.github.smiley4.schemakenerator.core.steps

import io.github.smiley4.schemakenerator.core.GenericBundleStep
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.flatten
import io.github.smiley4.schemakenerator.core.typedata.TypeData

/**
 * Adds missing subtype-supertype relations between the given types. Types not already present in the input are not included.
 * Example:
 * B is a subtype of A. B has A in its list of supertypes, but A not B as a subtype.
 * This step finds these missing connections and adds them.
 */
class AddMissingSubtypeSupertypeRelations : GenericBundleStep<TypeData, TypeData> {

    override fun process(input: Bundle<TypeData>): Bundle<TypeData> {
        val types = input.flatten()
        return input.also { schema ->
            addMissing(schema.data, types)
            schema.supporting.forEach { addMissing(it, types) }
        }
    }


    /**
     * Add the given type as supertype/subtype to other types in the given list
     */
    private fun addMissing(type: TypeData, dataList: List<TypeData>) {
        // check if this type is missing as the supertype in its subtypes
        type.subtypes.forEach { subtypeId ->
            val subtype = dataList.find { it.id == subtypeId }!!
            if (subtype.supertypes.none { it == type.id }) {
                subtype.supertypes.add(type.id)
            }
        }
        // check if this type is missing as a subtype in its supertype
        type.supertypes.forEach { supertypeId ->
            val supertype = dataList.find { it.id == supertypeId }!!
            if (supertype.subtypes.none { it == type.id }) {
                supertype.subtypes.add(type.id)
            }
        }
    }

}
