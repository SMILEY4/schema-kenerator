package io.github.smiley4.schemakenerator.core.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.flatten

/**
 * Adds missing subtype-supertype relations between the given types. Types not already present in the input are not included.
 * Example: B is a subtype of A. B has A in its list of supertypes, but A not B as a subtype. This step finds these missing connections and adds them.
 */
class ConnectSubTypesStep {

    /**
     * Adds missing subtype-supertype relations between the given types.
     */
    fun process(bundle: Bundle<BaseTypeData>): Bundle<BaseTypeData> {
        val types = bundle.flatten()
        return bundle.also { schema ->
            process(schema.data, types)
            schema.supporting.forEach { process(it, types) }
        }
    }

    private fun process(data: BaseTypeData, dataList: Collection<BaseTypeData>) {

        if (data is ObjectTypeData) {

            // check if this type is missing as the supertype in its subtypes
            data.subtypes.forEach { subtypeId ->
                val subtype = dataList.find { it.id == subtypeId }!!
                if (subtype is ObjectTypeData && subtype.supertypes.none { it == data.id }) {
                    subtype.supertypes.add(data.id)
                }
            }

            // check if this type is missing as a subtype in its supertype
            data.supertypes.forEach { supertypeId ->
                val supertype = dataList.find { it.id == supertypeId }!!
                if (supertype is ObjectTypeData && supertype.subtypes.none { it == data.id }) {
                    supertype.subtypes.add(data.id)
                }
            }

        }

    }

}