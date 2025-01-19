package io.github.smiley4.schemakenerator.core.typedata

fun Collection<TypeData>.find(id: TypeId): TypeData? {
    return this.find { it.id == id }
}

fun Collection<TypeData>.findOrThrow(id: TypeId): TypeData {
    return this.find(id) ?: throw IllegalArgumentException("No type data with id '$id' found.");
}

fun Collection<TypeParameterData>.find(name: String): TypeParameterData? {
    return this.find { it.name == name }
}

fun Collection<TypeParameterData>.findOrThrow(name: String): TypeParameterData {
    return this.find(name) ?: throw IllegalArgumentException("No type parameter with name '$name' found.");
}

fun Collection<MemberData>.findAnnotatedWith(annotationName: String): MemberData? {
    return this.find { member -> member.annotations.any { annotation -> annotation.name == annotationName } }
}

fun Collection<MemberData>.find(name: String): MemberData? {
    return this.find { it.name == name }
}


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
    )
}

fun TypeData.matches(
    other: TypeData,
    compareId: Boolean = true,
    compareIdentifyingName: Boolean = false,
    compareDescriptiveName: Boolean = false,
    compareTypeParameters: Boolean = false,
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
        if(this.typeParameters.zip(other.typeParameters).any { (a,b) -> a.type != b.type}) {
            return false
        }
    }

    return true
}