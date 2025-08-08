package org.mariella.persistence.kotlin.entities

import jakarta.persistence.*
import org.mariella.persistence.runtime.TrackedList
import java.time.Instant

@jakarta.persistence.Entity
@Table(name = "resource_node")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "node_type", discriminatorType = DiscriminatorType.STRING)
abstract class Resource : Entity() {
    @get:ManyToOne
    @get:JoinColumn(name = "space_id", referencedColumnName = "id")
    var space: Space? by changeSupport()

    @get:ManyToOne
    @get:JoinColumn(name = "owned_by", referencedColumnName = "id")
    var owner: UserEntity? by changeSupport()

    @get:ManyToOne
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

    @get:OneToMany(mappedBy = "resource")
    var resourceVersions: MutableList<ResourceVersion> = TrackedList(propertyChangeSupport, "resourceVersions")

    abstract fun isContainer(): Boolean
}