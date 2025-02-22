package io.github.smiley4.schemakenerator.core.data

object TypeDataUtils {

    /**
     * @return the [TypeData] with the given id or null
     */
    fun Collection<TypeData>.find(id: TypeId): TypeData? {
        return this.find { it.id == id }
    }


    /**
     * @return the [TypeData] with the given id. Throws if none exists.
     */
    fun Collection<TypeData>.findOrThrow(id: TypeId): TypeData {
        return this.find(id) ?: throw IllegalArgumentException("No type data with id '$id' found.");
    }


    /**
     * @return the [TypeParameterData] with the given name or null
     */
    fun Collection<TypeParameterData>.find(name: String): TypeParameterData? {
        return this.find { it.name == name }
    }


    /**
     * @return the [TypeParameterData] with the given name. Throws if none exists.
     */
    fun Collection<TypeParameterData>.findOrThrow(name: String): TypeParameterData {
        return this.find(name) ?: throw IllegalArgumentException("No type parameter with name '$name' found.");
    }


    /**
     * @return the [MemberData] with an annotation with the given name (or null).
     */
    fun Collection<MemberData>.findAnnotatedWith(annotationName: String): MemberData? {
        return this.find { member -> member.annotations.any { annotation -> annotation.name == annotationName } }
    }


    /**
     * @return the [MemberData] with the given name or null
     */
    fun Collection<MemberData>.find(name: String): MemberData? {
        return this.find { it.name == name }
    }


    /**
     * @return whether this type data matches the given data of another type data
     * @param identifyingName the identifying name of the other type data
     * @param descriptiveName the descriptive name of the other type data
     * @param typeParameters the type parameters of the other type data.
     */
    fun TypeData.matches(identifyingName: TypeName, descriptiveName: TypeName, typeParameters: List<TypeParameterData>): Boolean {
        return this.matches(
            TypeData(
                id = TypeId.createWildcard(),
                identifyingName = identifyingName,
                descriptiveName = descriptiveName,
                typeParameters = typeParameters.toMutableList(),
                annotations = mutableListOf(),
                subtypes = mutableListOf(),
                supertypes = mutableListOf(),
                members = mutableListOf(),
                isInlineValue = false,
                enumData = null,
                collectionData = null,
                mapData = null
            ),
            compareId = false,
            compareIdentifyingName = true,
            compareDescriptiveName = true,
            compareTypeParameters = true,
            compareMembers = false,
        )
    }


    /**
     * @return whether this type data matches the given other type data. Only compare specified attributes.
     */
    @Suppress("LongParameterList", "CyclomaticComplexMethod", "ReturnCount")
    fun TypeData.matches(
        other: TypeData,
        compareId: Boolean = true,
        compareIdentifyingName: Boolean = false,
        compareDescriptiveName: Boolean = false,
        compareTypeParameters: Boolean = false,
        compareMembers: Boolean = false,
    ): Boolean {

        if (compareId) {
            if (this.id != other.id) {
                return false
            }
        }

        if (compareIdentifyingName) {
            if (this.identifyingName.full != other.identifyingName.full || this.identifyingName.short != other.identifyingName.short) {
                return false
            }
        }

        if (compareDescriptiveName) {
            if (this.descriptiveName.full != other.descriptiveName.full || this.descriptiveName.short != other.descriptiveName.short) {
                return false
            }
        }

        if (compareTypeParameters) {
            if (this.typeParameters.size != other.typeParameters.size) {
                return false
            }
            if (this.typeParameters.zip(other.typeParameters).any { (a, b) -> a.type != b.type }) {
                return false
            }
        }

        if (compareMembers) {
            if (this.members.size != other.members.size) {
                return false
            }
            if (this.members.map { it.name }.toSet() != other.members.map { it.name }.toSet()) {
                return false
            }
            this.members.map { thisMember -> thisMember to other.members.find { it.name == thisMember.name }!! }
                .forEach { (thisMember, otherMember) ->
                    if (thisMember.type != otherMember.type) return@matches false
                    if (thisMember.nullable != otherMember.nullable) return@matches false
                    if (thisMember.optional != otherMember.optional) return@matches false
                    if (thisMember.kind != otherMember.kind) return@matches false
                    if (thisMember.visibility != otherMember.visibility) return@matches false
                }
        }

        return true
    }

}
