package io.github.smiley4.schemakenerator.parser.reflection

import io.github.smiley4.schemakenerator.getKType
import io.github.smiley4.schemakenerator.parser.core.TypeParser
import io.github.smiley4.schemakenerator.parser.core.TypeParsingConfig
import io.github.smiley4.schemakenerator.parser.core.TypeParsingContext
import io.github.smiley4.schemakenerator.parser.data.TypeRef
import kotlin.reflect.KClass
import kotlin.reflect.KType


class TypeReflectionParser(private val config: TypeParsingConfig, private val context: TypeParsingContext) : TypeParser {

    override fun getContext(): TypeParsingContext = context

    fun getConfig(): TypeParsingConfig = config

    fun getClassParser(): ClassParser = ClassParser(this)
    fun getTypeParameterParser(): TypeParameterParser = TypeParameterParser(this)
    fun getMemberParser(): MemberParser = MemberParser(this)
    fun getSupertypeParser(): SupertypeParser = SupertypeParser(this)
    fun getEnumValueParser(): EnumValueParser = EnumValueParser(this)

    // TODO: resolve annotations
    //  - all as raw additinal information ?
    //  - as programmable processing-step ? -> only include wanted annotations ? -> modify data?
    // TODO: make everything mutable + post-processing step

    inline fun <reified T> parse(): TypeRef = this.parse(getKType<T>())

    override fun parse(type: KType): TypeRef {
        if (type.classifier is KClass<*>) {
            return getClassParser().parse(type, type.classifier as KClass<*>, mapOf())
        } else {
            throw Exception("Type is not a class.")
        }
    }

}