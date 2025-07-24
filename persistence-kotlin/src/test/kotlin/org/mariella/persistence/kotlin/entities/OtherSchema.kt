package org.mariella.persistence.kotlin.entities

import javax.persistence.Column
import javax.persistence.Table

@javax.persistence.Entity
@Table(name = "other_schema", schema = "hansi")
class OtherSchema : Entity() {
    @get:Column(name = "name")
    var name: String by changeSupport()
}