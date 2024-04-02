package org.mariella.test.entities

import javax.persistence.Column
import javax.persistence.Table

@javax.persistence.Entity
@Table(name = "space")
class Space : Entity() {
    @get:Column(name = "name")
    var name: String by changeSupport()

    @Suppress("JpaAttributeTypeInspection")
    @get:Column(name = "security_concept")
    var securityConcept: SecurityConcept by changeSupport()
}