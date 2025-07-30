package org.mariella.persistence.kotlin.entities

import jakarta.persistence.Column
import jakarta.persistence.Table

@jakarta.persistence.Entity
@Table(name = "other_schema", schema = "hansi")
class OtherSchema : Entity() {
    @get:Column(name = "name")
    var name: String by changeSupport()
}