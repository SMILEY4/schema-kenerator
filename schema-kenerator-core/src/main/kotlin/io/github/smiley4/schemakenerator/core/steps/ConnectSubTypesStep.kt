package io.github.smiley4.schemakenerator.core.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
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
    fun process(data: Collection<BaseTypeData>): List<BaseTypeData> {
        return data.map { process(it, data) }
    }

    private fun process(data: BaseTypeData, dataList: Collection<BaseTypeData>): BaseTypeData {

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

        return data
    }

}