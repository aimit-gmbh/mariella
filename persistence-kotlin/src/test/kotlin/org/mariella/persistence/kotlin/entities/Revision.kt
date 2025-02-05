package org.mariella.persistence.kotlin.entities

import java.time.Instant
import javax.persistence.Column
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@javax.persistence.Entity
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