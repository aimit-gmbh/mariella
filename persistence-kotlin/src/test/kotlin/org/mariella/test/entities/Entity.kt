package org.mariella.test.entities

import org.mariella.persistence.kotlin.TrackingSupport
import java.time.Instant
import java.util.*
import javax.persistence.Column
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class Entity : TrackingSupport() {
    companion object {
        private const val MAX_DB_TIMESTAMP_MILLIS = 253402214400000
        val MAX_DB_TIMESTAMP: Instant = Instant.ofEpochMilli(MAX_DB_TIMESTAMP_MILLIS)
    }

    @get:Id
    @get:Column(name = "id")
    var id: UUID by changeSupport(UUID.randomUUID())
}