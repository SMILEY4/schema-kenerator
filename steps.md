# core

### addMissingSupertypeSubtypeRelations

Adds missing subtype-supertype relations between the given types. Types not already present in the input are not included.
Example:
B is a subtype of A. B has A in its list of supertypes, but A not B as a subtype.
This step finds these missing connections and adds them.
Add this step after type analysis and before schema generation.


### handleNameAnnotation

Changes the [TypeData.descriptiveName] to the name specified by a [Name]-annotation.
Add this step after type analysis and before schema generation.


### renameMembers

Renames members of types according to the given function.
Add this step after type analysis and before schema generation.


### gettersToProperties

Merges getters with their matching property:
 - if a matching property exists, the getter will be removed and relevant data copied to the property
 - if no property exists (e.g. because it is private), the getter will be removed and a new property from its data is created
Add this step after type analysis and before schema generation.

### addDiscriminatorProperty


Adds properties to types with subtypes used to differentiate between the possible subtypes when (de-)serializing.
The created property is annotated with a marker annotation with the name [AbstractAddDiscriminatorStep.MARKER_ANNOTATION_NAME].
If a type already contains a property annotated with the marker annotation, no new property will be added.
Add this step after type analysis and before schema generation.



# reflection

### collectSubTypes

Finds additional subtypes from [SubType]-annotation.
An additional step to add missing subtype-supertype relations .later may be required
Add this step before type analysis.

### analyseTypeUsingReflection

Analyze the type and using reflection and return the extracted data.



# serialization

### addJsonClassDiscriminatorProperty

Handles the [JsonClassDiscriminator]-annotations and adds a discriminator property with the defined name and
annotated with a marker annotation called [io.github.smiley4.schemakenerator.core.AbstractAddDiscriminatorStep.MARKER_ANNOTATION_NAME]


### renameMembers

Renames members of types according to the given [JsonNamingStrategy].
Add this step after type analysis and before schema generation.


### analyzeTypeUsingKotlinxSerialization

Analyze the type and using kotlinx-serialization and return the extracted data.



# jackson

# jackson-jsonschema

# jackson-swagger

# validation swagger

# jsonschema

# swagger
