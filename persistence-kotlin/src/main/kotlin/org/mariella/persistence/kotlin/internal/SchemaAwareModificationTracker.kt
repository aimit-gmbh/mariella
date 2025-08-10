package org.mariella.persistence.kotlin.internal

import org.mariella.persistence.loader.ModifiableFactory
import org.mariella.persistence.mapping.SchemaMapping
import org.mariella.persistence.runtime.ModifiableAccessor
import org.mariella.persistence.runtime.ModificationTracker
import org.mariella.persistence.runtime.ModificationTrackerImpl
import org.mariella.persistence.schema.ClassDescription

class SchemaAwareModificationTracker internal constructor(val schemaMapping: SchemaMapping, val factory: ModifiableFactory) :
    ModificationTracker by ModificationTrackerImpl(schemaMapping.schemaDescription) {

    inline fun <reified T> createNew(): T {
        val classDescription = schemaMapping.schemaDescription.getClassDescription(T::class.java.name)
        val instance = factory.createModifiable(classDescription)
        addNewParticipant(instance)
        return instance as T
    }

    inline fun <reified T> addExisting(id: Any): T {
        val classDescription = schemaMapping.schemaDescription.getClassDescription(T::class.java.name) ?: error("class description not found for ${T::class.java.name}")
        return applyClassDescription<T>(classDescription, id)
    }

    inline fun <reified T> applyClassDescription(classDescription: ClassDescription, id: Any): T {
        val instance = factory.createModifiable(classDescription)
        ModifiableAccessor.Singleton.setValue(instance, classDescription.getPropertyDescription("id"), id)
        addExistingParticipant(instance)
        return instance as T
    }

    inline fun <reified T> addExisting(id: Any, discriminator: String): T {
        val classDescription = schemaMapping.schemaDescription.getClassDescription(resolveSubclass<T>(discriminator))
        return applyClassDescription<T>(classDescription, id)
    }

    inline fun <reified T> resolveSubclass(discriminator: String): String {
        return schemaMapping.getClassMapping(T::class.java.name)
            .getClassMappingForDiscriminatorValue(discriminator).classDescription.className
    }
}