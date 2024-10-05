@file:Suppress("ClassName")

package io.github.smiley4.schemakenerator.examples

import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.kotest.core.spec.style.FreeSpec
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

class E02_Extract_Data_From_Types : FreeSpec({

    "extracting basic data from classes ..." - {

        @Serializable
        class SimpleClass(
            val text: String,
            val number: Int?
        )

        "... using reflection" {
            val extracted = typeOf<SimpleClass>().processReflection()

            // With the "processReflection()"-step, information about the given type is extracted and stored in "BaseTypeData" using jvm reflection features.
            // The result is extracted information about the type "SimpleClass" as well as other referenced types, i.e. "String" and "Int".
            // The data for the root type (i.e. "SimpleClass") is stored in "Bundle#data" and referenced types ("String", "Int") in "Bundle#supporting".

            println(extracted.data.simpleName)                  // -> "TestClass"
            println(extracted.supporting.map { it.simpleName }) // -> "[String, Int]"
        }

        "... using kotlinx-serialization" {
            val extracted = typeOf<SimpleClass>().processKotlinxSerialization()

            // The "processKotlinxSerialization()"-step fulfills the same role as "processReflection"-step, but uses the kotlinx-serialization library.
            // In general "processKotlinxSerialization()" has access to less/different information and the produces different results than reflection.

            println(extracted.data.simpleName)                  // -> "TestClass"
            println(extracted.supporting.map { it.simpleName }) // -> "[String, Int]"
        }

    }

    "dealing with inheritance" - {



    }

})