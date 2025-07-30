package org.mariella.persistence.kotlin.simple

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.mariella.persistence.kotlin.TrackingSupport
import java.util.*

@Table(name = "simple")
@Entity
class OtherSimpleEntity : TrackingSupport() {
    @get:Id
    @get:Column(name = "id")
    var id: UUID by changeSupport(UUID.randomUUID())

    @get:Column(name = "name")
    var name: String by changeSupport()

    @get:Column(name = "age")
    var age: Int by changeSupport()
}