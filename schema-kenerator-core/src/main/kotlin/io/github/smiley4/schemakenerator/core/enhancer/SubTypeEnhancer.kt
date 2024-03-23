package io.github.smiley4.schemakenerator.core.enhancer

import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.core.parser.idStr

class SubTypeEnhancer(private val inlineTypes: Boolean) : TypeDataEnhancer {

    override fun enhance(context: TypeDataContext) {
        context.getTypes()
            .asSequence()
            .filterIsInstance<ObjectTypeData>()
            .forEach { enrichSubTypes(it, context) }
    }

    private fun enrichSubTypes(type: ObjectTypeData, context: TypeDataContext) {
        val additionalSubTypes = context.getTypes()
            .asSequence()
            .filterIsInstance<ObjectTypeData>()
            .filter { it.supertypes.any { sup -> sup.idStr() == type.id.id } }
            .map {
                if (inlineTypes) {
                    InlineTypeRef(it)
                } else {
                    ContextTypeRef(it.id)
                }
            }
            .filter { !type.subtypes.map { sub -> sub.idStr() }.contains(it.idStr()) }
        type.subtypes.addAll(additionalSubTypes)
    }

}