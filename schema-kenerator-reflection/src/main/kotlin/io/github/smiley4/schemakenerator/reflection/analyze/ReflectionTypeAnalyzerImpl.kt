package io.github.smiley4.schemakenerator.reflection.analyze

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.InputType
import io.github.smiley4.schemakenerator.core.data.KTypeInput
import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.core.typedata.TypeId
import io.github.smiley4.schemakenerator.core.typedata.TypeParameterData
import io.github.smiley4.schemakenerator.core.typedata.WrappedTypeData
import io.github.smiley4.schemakenerator.core.typedata.matches
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class ReflectionTypeAnalyzerImpl(
    /**
     * redirect types to other types, i.e. when a type is found as a key, the corresponding type will be processed instead
     */
    private val typeRedirects: Map<KType, KType> = DEFAULT_REDIRECTS,
    /**
     * todo
     */
    private val modules: List<ReflectionTypeAnalyzerModule>

) : ReflectionTypeAnalyzer {

    companion object {

        enum class TypeCategory {
            PRIMITIVE,
            OBJECT,
            ENUM,
            COLLECTION,
            MAP
        }

        val DEFAULT_PRIMITIVE_TYPES = setOf<KClass<*>>(
            Number::class,
            Byte::class,
            Short::class,
            Int::class,
            Long::class,
            UByte::class,
            UShort::class,
            UInt::class,
            ULong::class,
            Float::class,
            Double::class,
            Boolean::class,
            Char::class,
            String::class,
            Any::class,
            Unit::class,
        )


        @OptIn(ExperimentalUnsignedTypes::class)
        val DEFAULT_REDIRECTS = mapOf(
            typeOf<BooleanArray>() to typeOf<Array<Boolean>>(),
            typeOf<ByteArray>() to typeOf<Array<Byte>>(),
            typeOf<UByteArray>() to typeOf<Array<UByte>>(),
            typeOf<ShortArray>() to typeOf<Array<Short>>(),
            typeOf<UShortArray>() to typeOf<Array<UShort>>(),
            typeOf<CharArray>() to typeOf<Array<Char>>(),
            typeOf<IntArray>() to typeOf<Array<Int>>(),
            typeOf<UIntArray>() to typeOf<Array<UInt>>(),
            typeOf<LongArray>() to typeOf<Array<Long>>(),
            typeOf<ULongArray>() to typeOf<Array<ULong>>(),
            typeOf<FloatArray>() to typeOf<Array<Float>>(),
            typeOf<DoubleArray>() to typeOf<Array<Double>>(),
        )
    }


    /**
     * Process the given input type
     */
    fun process(input: InputType): Bundle<TypeData> = process(Bundle(input, emptyList()))


    /**
     * Process the given input type bundle
     */
    fun process(input: Bundle<InputType>): Bundle<TypeData> {

        val knownTypeData = mutableListOf<TypeData>()

        // process supporting inputs
        input.supporting.forEach {
            when (it) {
                is KTypeInput -> process(it.kType, knownTypeData)
                else -> throw IllegalArgumentException("Unsupported input type '$it'.")
            }
        }

        // process main input
        val typeData = input.data.let {
            when (it) {
                is KTypeInput -> process(it.kType, knownTypeData)
                else -> throw IllegalArgumentException("Unsupported input type '$it'.")
            }
        }

        knownTypeData.remove(typeData.typeData)
        return Bundle(
            data = typeData.typeData,
            supporting = knownTypeData
        )
    }


    /**
     * Process the given type and adds new results to the given collection.
     * @param type the type to process
     * @param knownTypeData the already known type data. Adds new results to this collection.
     */
    private fun process(type: KType, knownTypeData: MutableList<TypeData>): WrappedTypeData {
        return if (typeRedirects.containsKey(type)) {
            process(typeRedirects[type]!!, knownTypeData)
        } else if (type.classifier is KClass<*>) {
            parseClass(type, type.classifier as KClass<*>, emptyList(), knownTypeData)
        } else {
            throw IllegalArgumentException("Type is not a class: '${type.classifier}'.")
        }
    }


    override fun analyze(
        type: KType,
        clazz: KClass<*>,
        knownTypeParameters: List<TypeParameterData>,
        knownTypeData: MutableList<TypeData>
    ): WrappedTypeData {
        return parseClass(type, clazz, knownTypeParameters, knownTypeData)
    }


    /**
     * Parses the given type as class and adds the results to the given collection
     * @param type the input type to parse
     * @param clazz the input class to parse
     * @param knownTypeParameters already parsed type parameter data
     * @param knownTypeData the already known type data. Adds new results to this collection.
     */
    private fun parseClass(
        type: KType,
        clazz: KClass<*>,
        knownTypeParameters: List<TypeParameterData>,
        knownTypeData: MutableList<TypeData>
    ): WrappedTypeData {

        // check type redirects
        if (typeRedirects.containsKey(type)) {
            return process(typeRedirects[type]!!, knownTypeData)
        }

        // find matching analyzer module for type
        val module = modules.first { it.matches() }

        // create minimal type data
        val id = TypeId.create()
        val minimalTypeData = module.preAnalyze(
            ReflectionTypeAnalyzerModule.Context(
                analyzer = this,
                id = id,
                type = type,
                clazz = clazz,
                knownTypeParameters = knownTypeParameters,
                knownTypeData = knownTypeData
            )
        )

        // check type has already been parsed
        val existing = knownTypeData.find { known ->
            known.matches(
                minimalTypeData.identifyingName,
                minimalTypeData.descriptiveName,
                minimalTypeData.typeParameters
            )
        }
        if (existing != null) {
            return WrappedTypeData(
                typeData = existing,
                nullable = type.isMarkedNullable,
            )
        }

        // add placeholder to break out of some infinite recursions
        knownTypeData.add(
            TypeData.createPlaceholder(
                id,
                minimalTypeData.identifyingName,
                minimalTypeData.descriptiveName,
                minimalTypeData.typeParameters
            )
        )

        // analyze type
        val wrappedTypeData = module.analyze(
            ReflectionTypeAnalyzerModule.Context(
                analyzer = this,
                id = id,
                type = type,
                clazz = clazz,
                knownTypeParameters = knownTypeParameters + minimalTypeData.typeParameters,
                knownTypeData = knownTypeData
            ),
            minimalTypeData
        )

        // handle analysis result
        return wrappedTypeData.also { result ->
            knownTypeData.removeIf {
                it.matches(
                    other = result.typeData,
                    compareId = false,
                    compareIdentifyingName = true,
                    compareDescriptiveName = true,
                    compareTypeParameters = true,
                    compareMembers = false
                )
            }
            knownTypeData.add(result.typeData)
        }
    }

}