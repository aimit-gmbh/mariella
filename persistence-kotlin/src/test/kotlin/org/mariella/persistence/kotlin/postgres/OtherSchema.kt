package org.mariella.persistence.kotlin.postgres

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "other_schema", schema = "hansi")
class OtherSchema : org.mariella.persistence.kotlin.entities.Entity() {
    @get:Column(name = "name")
    var name: String by changeSupport()
}