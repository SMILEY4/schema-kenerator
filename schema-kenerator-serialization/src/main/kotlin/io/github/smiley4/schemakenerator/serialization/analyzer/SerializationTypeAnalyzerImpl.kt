@file:OptIn(ExperimentalSerializationApi::class)

package io.github.smiley4.schemakenerator.serialization.analyzer

import io.github.smiley4.schemakenerator.core.data.InitialKTypeData
import io.github.smiley4.schemakenerator.core.data.InitialTypeData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.WrappedTypeData
import io.github.smiley4.schemakenerator.core.data.matches
import io.github.smiley4.schemakenerator.serialization.data.InitialSerialDescriptorTypeData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.capturedKClass
import kotlinx.serialization.descriptors.nonNullOriginal
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KType

internal class SerializationTypeAnalyzerImpl(
    /**
     * kotlinx serializers module from `Json { }.serializersModule` for support of contextual serializers
     */
    private val serializersModule: SerializersModule? = null,
    /**
     * redirect types to other types, i.e. when a type is found as a key, the corresponding type will be processed instead
     */
    private val typeRedirects: Map<String, SerialDescriptor> = emptyMap(),
    /**
     * List of modules for type analysis. First matching module is used to analyze a given type.
     */
    private val modules: List<SerializationTypeAnalyzerModule>
) : SerializationTypeAnalyzer {

    /**
     * Analyzes the given input type bundle
     */
    fun analyze(input: InitialTypeData): TypeDataGroup {

        val knownTypeData = mutableListOf<TypeData>()

        val root = when (input) {
            is InitialKTypeData -> {
                input.associatedTypes.forEach { analyze(it, knownTypeData) }
                analyze(input.type, knownTypeData)
            }
            is InitialSerialDescriptorTypeData -> {
                analyze(input.type, knownTypeData)
            }
            else -> throw IllegalArgumentException("Unsupported input type: ${input::class.qualifiedName}")
        }

        return TypeDataGroup(
            rootId = root.typeData.id,
            data = knownTypeData.associateBy { it.id },
        )
    }

    private fun analyze(input: KType, knownTypeData: MutableList<TypeData>): WrappedTypeData {
        return getSerializerFor(input)
            ?.let { serializer -> analyze(serializer.descriptor, input.isMarkedNullable, knownTypeData, mutableMapOf()) }
            ?: WrappedTypeData(
                typeData = knownTypeData.find(TypeId.createWildcard()) ?: TypeData.createWildcard().also { knownTypeData.add(it) },
                nullable = false
            )
    }

    private fun analyze(input: SerialDescriptor, knownTypeData: MutableList<TypeData>): WrappedTypeData {
        return analyze(input, input.isNullable, knownTypeData, mutableMapOf())
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
        processedDescriptors[descriptor.nonNullOriginal] = TypeData.createPlaceholder(reservedTypeId)

        // check type redirects
        if (typeRedirects.containsKey(descriptor.redirectKey(nullable))) {
            val redirectTo = typeRedirects[descriptor.redirectKey(nullable)]!!
            return analyze(redirectTo, knownTypeData).also {
                processedDescriptors[descriptor] = it.typeData
            }
        }

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
