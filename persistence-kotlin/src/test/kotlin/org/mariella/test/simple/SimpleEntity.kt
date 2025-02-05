package org.mariella.test.simple

import org.mariella.persistence.kotlin.TrackingSupport
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

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