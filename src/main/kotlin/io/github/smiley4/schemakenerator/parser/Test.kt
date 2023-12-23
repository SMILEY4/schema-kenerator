package io.github.smiley4.schemakenerator.parser

import io.github.smiley4.schemakenerator.parser.core.PrimitiveTypeData
import io.github.smiley4.schemakenerator.parser.reflection.ReflectionTypeParser
import io.github.smiley4.schemakenerator.parser.serialization.KotlinxSerializationTypeParser
import java.time.LocalDate
import java.time.LocalDateTime


class TestClassSimple(
    val someField: String
)

class TestClassNested(
    val myClass: TestClassSimple
)

@Suppress("DuplicatedCode")
fun main() {

    println("${TestClassNested::class.simpleName} > ${TestClassNested::myClass.name} > ${TestClassSimple::someField.name}")

    ReflectionTypeParser(
        config = {
            registerParser(LocalDate::class) { id, _ ->
                PrimitiveTypeData(
                    id = id,
                    simpleName = LocalDate::class.simpleName!!,
                    qualifiedName = LocalDate::class.qualifiedName!!,
                    typeParameters = emptyMap()
                )
            }
            registerParser(LocalDateTime::class) { id, _ ->
                PrimitiveTypeData(
                    id = id,
                    simpleName = LocalDate::class.simpleName!!,
                    qualifiedName = LocalDate::class.qualifiedName!!,
                    typeParameters = emptyMap()
                )
            }
        }
    )

    KotlinxSerializationTypeParser(
        config = {
            registerParser(LocalDate::class) { id, _ ->
                PrimitiveTypeData(
                    id = id,
                    simpleName = LocalDate::class.simpleName!!,
                    qualifiedName = LocalDate::class.qualifiedName!!,
                    typeParameters = emptyMap()
                )
            }
            registerParser(LocalDateTime::class) { id, _ ->
                PrimitiveTypeData(
                    id = id,
                    simpleName = LocalDate::class.simpleName!!,
                    qualifiedName = LocalDate::class.qualifiedName!!,
                    typeParameters = emptyMap()
                )
            }
        }
    )

}