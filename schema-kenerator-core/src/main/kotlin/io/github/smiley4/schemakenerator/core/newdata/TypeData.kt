package io.github.smiley4.schemakenerator.core.newdata

import java.util.UUID

class TypeData(
    val id: TypeId,
    val name: TypeName,
    val descriptiveName: TypeName
)


data class TypeId(val id: String) {
    companion object {
        fun create() = TypeId(UUID.randomUUID().toString())
    }
}


data class TypeName(
    val short: String,
    val full: String,
)

fun main() {

    println(UUID.randomUUID().toString())

    val stringTypeData = TypeData(
        id = TypeId.create(),
        name = TypeName(
            short = "String",
            full = "kotlin.String"
        ),
        descriptiveName = TypeName(
            short = "String",
            full = "kotlin.String"
        ),
    )

    val uuidTypeData = TypeData(
        id = TypeId.create(),
        name = TypeName(
            short = "String",
            full = "kotlin.String"
        ),
        descriptiveName = TypeName(
            short = "uuid",
            full = "uuid"
        ),
    )

}