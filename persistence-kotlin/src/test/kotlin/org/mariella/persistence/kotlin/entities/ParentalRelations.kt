package org.mariella.persistence.kotlin.entities

import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import org.mariella.persistence.runtime.TrackedList

@jakarta.persistence.Entity
@Table(name = "parental_relation")
class ParentalRelations : Entity() {
    @ManyToMany
    @JoinTable(
        name = "parental_input",
        joinColumns = [JoinColumn(name = "parental_relation_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "resource_version_id", referencedColumnName = "id")]
    )
    val inputs: MutableList<ResourceVersion> = TrackedList(propertyChangeSupport, "inputs")

    @ManyToMany
    @JoinTable(
        name = "parental_output",
        joinColumns = [JoinColumn(name = "parental_relation_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "resource_version_id", referencedColumnName = "id")]
    )
    val outputs: MutableList<ResourceVersion> = TrackedList(propertyChangeSupport, "outputs")

}