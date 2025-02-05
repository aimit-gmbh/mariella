package org.mariella.test.entities

import org.mariella.persistence.runtime.TrackedList
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table

@javax.persistence.Entity
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