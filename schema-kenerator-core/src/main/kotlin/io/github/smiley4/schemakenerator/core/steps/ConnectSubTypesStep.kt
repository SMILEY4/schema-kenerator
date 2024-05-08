package io.github.smiley4.schemakenerator.core.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData

/**
 * Adds missing subtype-supertype relations between the given types. Types not present in the input list are not included.
 * - input: [BaseTypeData]
 * - output: same [BaseTypeData] with added [ObjectTypeData.subtypes] and [ObjectTypeData.supertypes]
 */
class ConnectSubTypesStep {

    /**
     * Adds missing subtype-supertype relations between the given types
     */
    fun process(bundle: Bundle<BaseTypeData>): Bundle<BaseTypeData> {
        val types = listOf(bundle.data) + bundle.supporting
        return bundle.also { schema ->
            process(schema.data, types)
            schema.supporting.forEach { process(it, types) }
        }
    }

    private fun process(data: BaseTypeData, dataList: Collection<BaseTypeData>) {

        if (data is ObjectTypeData) {

            data.subtypes.forEach { subtypeId ->
                val subtype = dataList.find { it.id == subtypeId }!!
                if (subtype is ObjectTypeData && subtype.supertypes.none { it == data.id }) {
                    subtype.supertypes.add(data.id)
                }
            }

            data.supertypes.forEach { supertypeId ->
                val supertype = dataList.find { it.id == supertypeId }!!
                if (supertype is ObjectTypeData && supertype.subtypes.none { it == data.id }) {
                    supertype.subtypes.add(data.id)
                }
            }

        }

    }

}