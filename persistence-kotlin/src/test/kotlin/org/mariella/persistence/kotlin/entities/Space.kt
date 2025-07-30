package org.mariella.persistence.kotlin.entities

import jakarta.persistence.Column
import jakarta.persistence.Table

@jakarta.persistence.Entity
@Table(name = "space")
class Space : Entity() {
    @get:Column(name = "name")
    var name: String by changeSupport()

    @Suppress("JpaAttributeTypeInspection")
    @get:Column(name = "security_concept")
    var securityConcept: SecurityConcept by changeSupport()
}