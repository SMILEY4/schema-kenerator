package io.github.smiley4.schemakenerator.parser.reflection

import io.github.smiley4.schemakenerator.getKType
import io.github.smiley4.schemakenerator.parser.core.TypeId
import io.github.smiley4.schemakenerator.parser.core.TypeParser
import io.github.smiley4.schemakenerator.parser.core.TypeParserContext
import kotlin.reflect.KClass
import kotlin.reflect.KType


class ReflectionTypeParser(
    config: ReflectionTypeParserConfigBuilder.() -> Unit = {},
    context: TypeParserContext = TypeParserContext()
) : TypeParser<ReflectionTypeParserConfig>(ReflectionTypeParserConfigBuilder().apply(config).build(), context) {

    fun getClassParser(): ClassParser = ClassParser(this)
    fun getTypeParameterParser(): TypeParameterParser = TypeParameterParser(this)
    fun getPropertyParser(): PropertyParser = PropertyParser(this)
    fun getSupertypeParser(): SupertypeParser = SupertypeParser(this)
    fun getEnumValueParser(): EnumValueParser = EnumValueParser(this)

    override fun parse(type: KType): TypeId {
        if (type.classifier is KClass<*>) {
            return getClassParser().parse(type, type.classifier as KClass<*>, mapOf())
        } else {
            throw Exception("Type is not a class.")
        }
    }

    inline fun <reified T> parse() = parse(getKType<T>())

}