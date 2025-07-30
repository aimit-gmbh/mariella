package org.mariella.persistence.kotlin.entities

import jakarta.persistence.Column
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.Table

@jakarta.persistence.Entity
@Table(name = "resource_character")
@Inheritance(strategy = InheritanceType.JOINED)
open class ResourceCharacter : Entity() {

    @get:Column(name = "name")
    var name: String by changeSupport()

    @get:Column(name = "scope")
    var scope: String by changeSupport()

}