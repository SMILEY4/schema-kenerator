@file:Suppress("ClassName")
@file:OptIn(ExperimentalSerializationApi::class)

package io.github.smiley4.schemakenerator.examples

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.smiley4.schemakenerator.core.addDiscriminatorProperty
import io.github.smiley4.schemakenerator.core.connectSubTypes
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.jackson.addJacksonTypeInfoDiscriminatorProperty
import io.github.smiley4.schemakenerator.jackson.collectJacksonSubTypes
import io.github.smiley4.schemakenerator.reflection.collectSubTypes
import io.github.smiley4.schemakenerator.reflection.data.EnumConstType
import io.github.smiley4.schemakenerator.reflection.data.SubType
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.serialization.addJsonClassDiscriminatorProperty
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.kotest.core.spec.style.FreeSpec
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.reflect.typeOf

class E02_Extracting_Data_From_Types : FreeSpec({

    "extracting basic data from classes ..." - {

        "... using reflection" {
            val extracted = typeOf<SimpleClass>().processReflection()

            // With the "processReflection()"-step, information about the given type is extracted and stored in "BaseTypeData" using jvm reflection features.
            // The result is extracted information about the type "SimpleClass" as well as other referenced types, i.e. "String" and "Int".
            // The data for the root type (i.e. "SimpleClass") is stored in "Bundle#data" and referenced types ("String", "Int") in "Bundle#supporting".

            println(extracted.data.simpleName)                  // -> "SimpleClass"
            println(extracted.supporting.map { it.simpleName }) // -> "[String, Int]"
        }

        "configuring the reflection step" {
            // The "processReflection()"-step has some parameters to configure its behavior and what information to include
            typeOf<SimpleClass>().processReflection {
                // whether to include getter functions as members of a type
                includeGetters = false
                // whether to include weak getter functions (i.e. function same as getters but do not start with "get...()")  as members of a type
                includeWeakGetters = false
                // whether to include functions that are not getters or weak getters as members of a types
                includeFunctions = false
                // whether to include hidden (e.g. private) fields, properties and functions
                includeHidden = false
                // whether to include static fields, properties and functions
                includeStatic = false
                // whether to extract enum constants by their name or by the output of their "toString"-method
                enumConstType = EnumConstType.NAME
            }
        }

        "... using kotlinx-serialization" {
            val extracted = typeOf<SimpleClass>().processKotlinxSerialization()

            // The "processKotlinxSerialization()"-step fulfills the same role as "processReflection"-step, but uses the kotlinx-serialization library.
            // In general "processKotlinxSerialization()" has access to less/different information and the produces different results than reflection.

            println(extracted.data.simpleName)                  // -> "SimpleClass"
            println(extracted.supporting.map { it.simpleName }) // -> "[String, Int]"
        }

        "configuring the kotlinx-serialization step" {
            // The "processKotlinxSerialization()"-step has parameters to configure its behavior
            typeOf<SimpleClass>().processKotlinxSerialization {
                // kotlinx-serialization looses some information about type parameters. Because of this, the "processKotlinxSerialization()"-step
                // has to treat types more carefully to avoid collisions and types overwriting other types with different type parameters.
                // This more careful behaviour can be disabled for specific types that are known to not have any
                // type parameters with the "markNotParameterized" configuration option.
                // This configuration may not be required for most situations, but can be used to manually resolve issues.
                markNotParameterized<SimpleClass>()
                markNotParameterized(typeOf<SimpleClass>())
                markNotParameterized(SimpleClass::class.qualifiedName!!)
            }
        }

    }

    "dealing with inheritance and subtypes" - {

        "finding and including subtypes" - {

            "sealed classes/interfaces" {

                // @Serializable
                // sealed class SealedParent {
                //     @Serializable class ChildOne : SealedParent()
                //     @Serializable class ChildTwo : SealedParent()
                // }

                val extractedReflection = typeOf<SealedParent>().processReflection()
                val extractedKotlinx = typeOf<SealedParent>().processKotlinxSerialization()

                // Subtypes of sealed classes and interfaces are detected and extracted automatically, regardless of reflection or kotlinx-serialization.

                println(extractedReflection.data.simpleName + ": " + (extractedReflection.data as ObjectTypeData).subtypes.map { it.simple() })  // -> "SealedParent: [ChildOne, ChildTwo]"
                println(extractedKotlinx.data.simpleName + ": " + (extractedKotlinx.data as ObjectTypeData).subtypes.map { it.simple() })        // -> "SealedParent: [ChildOne, ChildTwo]"

                // The detected subtypes are added to Bundle#supporting and are referenced by the parent-type via their ids

                println(extractedReflection.data.simpleName)                  // -> "SealedParent"
                println(extractedReflection.supporting.map { it.simpleName }) // -> "[ChildOne, ChildTwo]"

            }

            "manually including subtypes" {

                // When using classes that are not "sealed", subtypes are not automatically detected and have to be added to the "known types" for them to be included.

                // open class ParentManual {
                //     class ChildOne : ParentManual()
                //     class ChildTwo : ParentManual()
                // }

                val extracted = Bundle(
                    data = typeOf<ParentManual>(), // the main type we want to extract information from
                    supporting = listOf(
                        typeOf<ParentManual.ChildOne>(), // additional types we want to include when extracting information
                        typeOf<ParentManual.ChildTwo>()
                    )
                )
                    // extract information from all "known" types individually, i.e. from "ParentManual", "ChildOne", "ChildTwo"
                    .processReflection()
                    // "ChildOne" and "ChildTwo" reference "ParentManual" as their supertype, but "ParentManual" does not yet know about its subtypes.
                    // The "connectSubTypes()"-step finds and fills in these missing connections, i.e. adds "ChildOne" and "ChildTwo" to the subtypes of "ParentManual".
                    .connectSubTypes()

                println(extracted.data.simpleName + ": " + (extracted.data as ObjectTypeData).subtypes.map { it.simple() })           // -> "ParentManual: [ChildOne, ChildTwo]"
                extracted.supporting.forEach { supporting -> println((supporting as ObjectTypeData).supertypes.map { it.simple() }) } // -> "[ParentManual]", "[ParentManual]"

            }

            "schema-kenerator-core @SubType-annotation" {

                // Instead of adding the possible subtypes to the input manually, they can be collected automatically using the schema-kenerator-core "@SubType"-annotation.
                // This works best when using reflection, since the "@SubType"-annotation is not available to kotlinx-serialization.

                // @SubType(ParentCore.ChildOne::class)
                // @SubType(ParentCore.ChildTwo::class)
                // open class ParentCore {
                //     class ChildOne : ParentCore()
                //     class ChildTwo : ParentCore()
                // }

                val extracted = typeOf<ParentCore>()
                    // the "collectSubTypes()"-step looks at the @SubType-annotations present on any type (recursive) and adds the referenced types to be included in the next steps.
                    .collectSubTypes()
                    .processReflection()
                    // "ChildOne" and "ChildTwo" reference "ParentManual" as their supertype, but "ParentManual" does not yet know about its subtypes.
                    // The "connectSubTypes()"-step finds and fills in these missing connections, i.e. adds "ChildOne" and "ChildTwo" to the subtypes of "ParentManual".
                    .connectSubTypes()

                println(extracted.data.simpleName + ": " + (extracted.data as ObjectTypeData).subtypes.map { it.simple() })           // -> "ParentCore: [ChildOne, ChildTwo]"
                extracted.supporting.forEach { supporting -> println((supporting as ObjectTypeData).supertypes.map { it.simple() }) } // -> "[ParentCore]", "[ParentCore]"
            }

            "jackson @JsonSubTypes-annotation" {

                // Instead of adding the possible subtypes to the input manually, they can be collected automatically using the Jackson "@JsonSubTypes"-annotation.
                // This works best when using reflection, since the "@JsonSubTypes"-annotation is not available to kotlinx-serialization.

                // @JsonSubTypes(
                //     JsonSubTypes.Type(value = ParentJackson.ChildOne::class),
                //     JsonSubTypes.Type(value = ParentJackson.ChildTwo::class),
                // )
                // open class ParentJackson {
                //     class ChildOne : ParentJackson()
                //     class ChildTwo : ParentJackson()
                // }

                val extracted = typeOf<ParentJackson>()
                    // the "collectJacksonSubTypes()"-step looks at the @JsonSubTypes-annotation present on any type (recursive) and adds the referenced types to be included in the next steps.
                    .collectJacksonSubTypes({
                        it.processReflection() // this step needs to intermediate information from the types. This specifies how this information is extracted. reflection is reccomended here.
                    })
                    .processReflection()
                    // "ChildOne" and "ChildTwo" reference "ParentManual" as their supertype, but "ParentManual" does not yet know about its subtypes.
                    // The "connectSubTypes()"-step finds and fills in these missing connections, i.e. adds "ChildOne" and "ChildTwo" to the subtypes of "ParentManual".
                    .connectSubTypes()

                println(extracted.data.simpleName + ": " + (extracted.data as ObjectTypeData).subtypes.map { it.simple() })           // -> "ParentJackson: [ChildOne, ChildTwo]"
                extracted.supporting.forEach { supporting -> println((supporting as ObjectTypeData).supertypes.map { it.simple() }) } // -> "[ParentJackson]", "[ParentJackson]"
            }

        }

        "discriminator property" - {

            // A discriminator is used to differentiate between subtypes when (de-)serializing types, i.e. to know which subtype we are dealing with.
            // This property is often times not included as a field in the class itself, but configured e.g. via an annotation.
            // This property can also be added to the type information, e.g. to be later included in generated schemas.

            "default property" {

                // A default discriminator property with a specified name can be added to all types that have known subtypes. This works the same with kotlinx-serialization and reflection.

                // @Serializable
                // sealed class SealedParent {
                //     @Serializable class ChildOne : SealedParent()
                //     @Serializable class ChildTwo : SealedParent()
                // }

                val extracted = typeOf<SealedParent>()
                    .processKotlinxSerialization()
                    .addDiscriminatorProperty("_type") // adds a property "_type" to all types with subtypes

                val discriminatorProperty = (extracted.data as ObjectTypeData).members.find { it.name == "_type" }!!
                println(discriminatorProperty.name)                        // -> "_type"
                println(discriminatorProperty.type.full())                 // -> "kotlin.String"
                println(discriminatorProperty.annotations.map { it.name }) // -> "[discriminator_marker]"

                // An "annotation" with name "discriminator_marker" is added to the property to mark it and make it possible to find it later, e.g. when generating schemas.
                // If a property with the name already exists, the maker annotation is added to this property instead.
                // If a (different) property with marked with the annotation is already present, the step will NOT add the new specified property.
            }

            "kotlinx-serialization @JsonClassDiscriminator-annotation" {

                // A discriminator property with a name specified with the @JsonClassDiscriminator-annotation can be added to all types that have known subtypes.
                // This works the same with kotlinx-serialization and reflection.

                // @Serializable
                // @JsonClassDiscriminator("_type")
                // sealed class ParentDiscriminatorKotlinx {
                //     @Serializable class ChildOne : SealedParent()
                //     @Serializable class ChildTwo : SealedParent()
                // }

                val extracted = typeOf<ParentDiscriminatorKotlinx>()
                    .processKotlinxSerialization()
                    .addJsonClassDiscriminatorProperty() // adds a property with the name specified in @JsonClassDiscriminator to all annotated types with subtypes

                val discriminatorProperty = (extracted.data as ObjectTypeData).members.find { it.name == "_type" }!!
                println(discriminatorProperty.name)                        // -> "_type"
                println(discriminatorProperty.type.full())                 // -> "kotlin.String"
                println(discriminatorProperty.annotations.map { it.name }) // -> "[discriminator_marker]"

                // An "annotation" with name "discriminator_marker" is added to the property to mark it and make it possible to find it later, e.g. when generating schemas.
                // If a property with the name already exists, the maker annotation is added to this property instead.
                // If a (different) property with marked with the annotation is already present, the step will NOT add the new specified property.
            }

            "jackson @JsonTypeInfo-annotation" {

                // A discriminator property with a name specified with the @JsonClassDiscriminator-annotation can be added to all types that have known subtypes.
                // This works only when using reflection, since the "@JsonTypeInfo"-annotation is not available to kotlinx-serialization.
                // only @JsonTypeInfo#include "PROPERTY" and "EXISTING_PROPERTY" is supported.

                // @JsonTypeInfo(
                //     use = JsonTypeInfo.Id.NAME,
                //     include = JsonTypeInfo.As.PROPERTY,
                //     property = "_type"
                // )
                // sealed class ParentDiscriminatorJackson {
                //     class ChildOne : SealedParent()
                //     class ChildTwo : SealedParent()
                // }

                val extracted = typeOf<ParentDiscriminatorJackson>()
                    .processReflection()
                    .addJacksonTypeInfoDiscriminatorProperty() // adds a property with the name specified in @JsonTypeInfo#property to all annotated types with subtypes

                val discriminatorProperty = (extracted.data as ObjectTypeData).members.find { it.name == "_type" }!!
                println(discriminatorProperty.name)                        // -> "_type"
                println(discriminatorProperty.type.full())                 // -> "kotlin.String"
                println(discriminatorProperty.annotations.map { it.name }) // -> "[discriminator_marker]"

                // An "annotation" with name "discriminator_marker" is added to the property to mark it and make it possible to find it later, e.g. when generating schemas.
                // If a property with the name already exists, the maker annotation is added to this property instead.
                // If a (different) property with marked with the annotation is already present, the step will NOT add the new specified property.
            }

        }

    }

}) {

    companion object {

        @Serializable
        class SimpleClass(
            val text: String,
            val number: Int?
        )

        @Serializable
        sealed class SealedParent {
            @Serializable
            class ChildOne : SealedParent()
            @Serializable
            class ChildTwo : SealedParent()
        }

        open class ParentManual {
            class ChildOne : ParentManual()
            class ChildTwo : ParentManual()
        }


        @SubType(ParentCore.ChildOne::class)
        @SubType(ParentCore.ChildTwo::class)
        open class ParentCore {
            class ChildOne : ParentCore()
            class ChildTwo : ParentCore()
        }


        @JsonSubTypes(
            JsonSubTypes.Type(value = ParentJackson.ChildOne::class),
            JsonSubTypes.Type(value = ParentJackson.ChildTwo::class),
        )
        open class ParentJackson {
            class ChildOne : ParentJackson()
            class ChildTwo : ParentJackson()
        }


        @Serializable
        @JsonClassDiscriminator("_type")
        sealed class ParentDiscriminatorKotlinx {
            @Serializable
            class ChildOne : ParentDiscriminatorKotlinx()
            @Serializable
            class ChildTwo : ParentDiscriminatorKotlinx()
        }


        @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "_type"
        )
        sealed class ParentDiscriminatorJackson {
            class ChildOne : ParentDiscriminatorJackson()
            class ChildTwo : ParentDiscriminatorJackson()
        }

    }

}
