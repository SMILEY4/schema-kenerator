package io.github.smiley4.schemakenerator.parser

import io.github.smiley4.schemakenerator.parser.core.PrimitiveTypeData
import io.github.smiley4.schemakenerator.parser.reflection.ReflectionTypeParser
import io.github.smiley4.schemakenerator.parser.serialization.KotlinxSerializationTypeParser
import java.time.LocalDate
import java.time.LocalDateTime


@Suppress("DuplicatedCode")
fun main() {

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