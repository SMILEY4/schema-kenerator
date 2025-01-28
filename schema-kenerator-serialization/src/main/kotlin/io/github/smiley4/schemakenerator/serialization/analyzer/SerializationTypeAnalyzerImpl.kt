@file:OptIn(ExperimentalSerializationApi::class)

package io.github.smiley4.schemakenerator.serialization.analyzer

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.InputType
import io.github.smiley4.schemakenerator.core.data.KTypeInput
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.WrappedTypeData
import io.github.smiley4.schemakenerator.core.data.matches
import io.github.smiley4.schemakenerator.serialization.data.SerialDescriptorInput
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.capturedKClass
import kotlinx.serialization.descriptors.nonNullOriginal
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KType

internal class SerializationTypeAnalyzerImpl(
    /**
     * kotlinx serializers module from `Json { }.serializersModule` for support of contextual serializers
     */
    private val serializersModule: SerializersModule? = null,
    /**
     * redirect types to other types, i.e. when a type is found as a key, the corresponding type will be processed instead
     */
    private val typeRedirects: Map<String, InputType> = emptyMap(),
    /**
     * List of modules for type analysis. First matching module is used to analyze a given type.
     */
    private val modules: List<SerializationTypeAnalyzerModule>
) : SerializationTypeAnalyzer {

    /**
     * Analyzes the given input type
     */
    fun analyze(input: InputType): Bundle<TypeData> = analyze(Bundle(input, emptyList()))


    /**
     * Analyzes the given input type bundle
     */
    fun analyze(input: Bundle<InputType>): Bundle<TypeData> {

        val knownTypeData = mutableListOf<TypeData>()

        // process supporting inputs
        input.supporting.forEach {
            analyze(it, knownTypeData)
        }

        // process main input
        val typeData = analyze(input.data, knownTypeData)

        knownTypeData.remove(typeData.typeData)
        return Bundle(
            data = typeData.typeData,
            supporting = knownTypeData
        )
    }

    private fun analyze(input: InputType, knownTypeData: MutableList<TypeData>): WrappedTypeData {
        return when (input) {
            is KTypeInput -> getSerializer(input.kType)
                ?.let { serializer -> analyze(serializer.descriptor, input.kType.isMarkedNullable, knownTypeData, mutableMapOf()) }
                ?: WrappedTypeData(
                    typeData = knownTypeData.find(TypeId.createWildcard()) ?: TypeData.createWildcard(),
                    nullable = false
                )
            is SerialDescriptorInput -> analyze(input.descriptor, input.descriptor.isNullable, knownTypeData, mutableMapOf())
            else -> throw IllegalArgumentException("Unsupported input type '$input'.")
        }
    }


    /**
     * Get the serializer (or null) for the given type
     */
    private fun getSerializer(type: KType): KSerializer<Any?>? {
        return if (type.classifier is KClass<*>) {
            try {
                serializerOrNull(type)
            } catch (ignore: IllegalArgumentException) {
                null
            }
        } else {
            throw IllegalArgumentException("Type '$type' is not a class.")
        }
    }


    /**
     * Analyses the given descriptor and adds the results to the given collection
     * @param descriptor the input descriptor to parse
     * @param nullable whether the input descriptor is (externally) marked as nullable
     * @param knownTypeData the already known type data. Adds new results to this collection.
     * @param processedDescriptors already processed descriptors with their type data. Adds new results to this map.
     */
    override fun analyze(
        descriptor: SerialDescriptor,
        nullable: Boolean,
        knownTypeData: MutableList<TypeData>,
        processedDescriptors: MutableMap<SerialDescriptor, TypeData>
    ): WrappedTypeData {

        // input serial descriptor has already been parsed before (or is currently being parsed) -> break out of infinite loops
        if (processedDescriptors.containsKey(descriptor.nonNullOriginal)) {
            return WrappedTypeData(
                typeData = processedDescriptors[descriptor.nonNullOriginal]!!,
                nullable = descriptor != descriptor.nonNullOriginal
            )
        }

        // reserve this descriptor / mark this descriptor as processed with a pending result
        // reserve type-id so that other types can already reference this type (e.g. members resulting in a reference loop)
        val reservedTypeId = TypeId.create()
        processedDescriptors[descriptor] = TypeData.createPlaceholder(reservedTypeId)

        // check type redirects
        if (typeRedirects.containsKey(descriptor.redirectKey(nullable))) {
            val redirectTo = typeRedirects[descriptor.redirectKey(nullable)]!!
            return analyze(redirectTo, knownTypeData).also {
                processedDescriptors[descriptor] = it.typeData
            }
        }

        // todo: custom already here ? or here aswell ? i.e. before contextual ?

        // check contextual descriptors
        val contextualByKClass = descriptor.capturedKClass?.let { serializersModule?.getContextual(it)?.descriptor }
        if (contextualByKClass != null) {
            return analyze(contextualByKClass, nullable, knownTypeData, processedDescriptors)
        }

        // find matching analyzer module for type
        val module = modules.firstOrNull { it.applies(descriptor) }
            ?: throw IllegalArgumentException("No analysis module matches the given serial descriptor '$descriptor'.")


        // analyze type
        val wrappedTypeData = module.analyze(
            SerializationTypeAnalyzerModule.Context(
                analyzer = this,
                id = reservedTypeId,
                descriptor = descriptor,
                nullable = nullable,
                knownTypeData = knownTypeData,
                processedDescriptors = processedDescriptors
            )
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
                    compareMembers = true
                )
            }
            knownTypeData.add(result.typeData)
            processedDescriptors[descriptor] = result.typeData
        }
    }


    /**
     * @return a key used to match redirect types
     */
    private fun SerialDescriptor.redirectKey(nullable: Boolean) = fullName() + if (nullable || this.isNullable) "?" else ""


    /**
     * @return the type data with the given id or null
     */
    private fun Collection<TypeData>.find(id: TypeId): TypeData? = this.find { it.id == id }

}
