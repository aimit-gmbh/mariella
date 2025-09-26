package org.mariella.persistence.kotlin.entities

import jakarta.persistence.*
import jakarta.persistence.Entity
import org.mariella.persistence.kotlin.TrackingSupport

@Entity
@Table(name = "ntf_record")
class RecordEntity : TrackingSupport() {
    @get:Id
    @get:Column(name = "id")
    @get:GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int? by changeSupport()

    @get:Column("notification_type")
    var type: String by changeSupport()
}