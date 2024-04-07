package io.github.smiley4.schemakenerator.reflection.parsers

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeParser
import io.github.smiley4.schemakenerator.core.parser.TypeRef
import io.github.smiley4.schemakenerator.reflection.ReflectionTypeParserConfig
import io.github.smiley4.schemakenerator.reflection.ReflectionTypeParserConfigBuilder
import io.github.smiley4.schemakenerator.reflection.getKType
import kotlin.reflect.KClass
import kotlin.reflect.KType


class ReflectionTypeParser(
    config: ReflectionTypeParserConfigBuilder.() -> Unit = {},
    context: TypeDataContext = TypeDataContext()
) : TypeParser<ReflectionTypeParserConfig>(ReflectionTypeParserConfigBuilder().apply(config).build(), context) {

    fun getClassParser(): ClassParser = ClassParser(this)
    fun getTypeParameterParser(): TypeParameterParser = TypeParameterParser(this)
    fun getPropertyParser(): PropertyParser = PropertyParser(this)
    fun getSupertypeParser(): SupertypeParser = SupertypeParser(this)
    fun getEnumValueParser(): EnumValueParser = EnumValueParser(this)
    fun getAnnotationParser(): AnnotationParser = AnnotationParser(this)

    inline fun <reified T> parse() = parse(getKType<T>())

    override fun parse(type: KType): TypeRef {
        if (config.clearContext) {
            context.clear()
        }
        if (type.classifier is KClass<*>) {
            return getClassParser().parse(type, type.classifier as KClass<*>, mapOf())
        } else {
            throw Exception("Type is not a class.")
        }
    }

    internal fun parseCustom(id: TypeId, clazz: KClass<*>): BaseTypeData? {
        val customParser = config.customParsers[clazz] ?: config.customParser
        if (customParser != null) {
            return customParser.parse(id, clazz)
        }
        return null
    }

}