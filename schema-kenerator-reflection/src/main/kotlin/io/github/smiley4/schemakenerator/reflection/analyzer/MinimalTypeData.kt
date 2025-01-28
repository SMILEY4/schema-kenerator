package io.github.smiley4.schemakenerator.reflection.analyzer

import io.github.smiley4.schemakenerator.core.data.TypeName
import io.github.smiley4.schemakenerator.core.data.TypeParameterData

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
