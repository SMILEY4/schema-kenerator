package io.github.smiley4.schemakenerator.reflection.data

import io.github.smiley4.schemakenerator.core.typedata.TypeName
import io.github.smiley4.schemakenerator.core.typedata.TypeParameterData

data class MinimalTypeData(
    /**
     * the name of this type.
     */
    val identifyingName: TypeName,
    /**
     * A possibly more descriptive name of this type.
     */
    val descriptiveName: TypeName,
    /**
     * the type parameters (i.e. generics) of this type
     */
    val typeParameters: List<TypeParameterData> = mutableListOf(),
)
