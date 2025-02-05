package org.mariella.persistence.kotlin.entities

import java.time.Instant
import javax.persistence.*

@javax.persistence.Entity
@Table(name = "resource_node")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "node_type", discriminatorType = DiscriminatorType.STRING)
abstract class Resource : Entity() {
    @get:OneToOne
    @get:JoinColumn(name = "space_id", referencedColumnName = "id")
    var space: Space? by changeSupport()

    @get:OneToOne
    @get:JoinColumn(name = "owned_by", referencedColumnName = "id")
    var owner: UserEntity? by changeSupport()

    @get:OneToOne
    @get:JoinColumn(name = "revision_id", referencedColumnName = "id")
    var revision: Revision? by changeSupport()

    @get:Column(name = "node_comment")
    var comment: String? by changeSupport()

    @get:Column(name = "revision_time")
    var createdAt: Instant by changeSupport()

    @get:Column(name = "locked_at")
    var lockedAt: Instant? by changeSupport()

    @get:Column(name = "entity_id")
    var entityId: String by changeSupport()

    abstract fun isContainer(): Boolean
}