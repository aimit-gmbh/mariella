package org.mariella.persistence.kotlin.entities

import jakarta.persistence.Column
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant

@jakarta.persistence.Entity
@Table(name = "revision")
class Revision : Entity() {
    @get:OneToOne
    @get:JoinColumn(name = "space_id", referencedColumnName = "id")
    var space: Space? by changeSupport()

    @get:OneToOne
    @get:JoinColumn(name = "created_by", referencedColumnName = "id")
    var creationUser: UserEntity? by changeSupport()

    @get:Column(name = "created_at")
    var createdAt: Instant by changeSupport()
}