package org.mariella.persistence.kotlin.entities

import jakarta.persistence.*

@jakarta.persistence.Entity
@Table(name = "auth_member")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "obj_type", discriminatorType = DiscriminatorType.STRING)
abstract class Member : Entity() {
    @get:Column(name = "status")
    var deleted: Boolean by changeSupport(false)
}