package org.mariella.persistence.kotlin.entities

import javax.persistence.Column
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.Table

@javax.persistence.Entity
@Table(name = "resource_character")
@Inheritance(strategy = InheritanceType.JOINED)
open class ResourceCharacter : Entity() {

    @get:Column(name = "name")
    var name: String by changeSupport()

    @get:Column(name = "scope")
    var scope: String by changeSupport()

}