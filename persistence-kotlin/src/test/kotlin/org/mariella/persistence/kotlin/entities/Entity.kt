package org.mariella.persistence.kotlin.entities

import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.mariella.persistence.kotlin.TrackingSupport
import java.time.Instant
import java.util.*

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