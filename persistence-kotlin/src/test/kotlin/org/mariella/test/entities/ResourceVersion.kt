package org.mariella.test.entities

import java.time.Instant
import javax.persistence.*

@javax.persistence.Entity
@Table(name = "resource_node_version")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "node_type", discriminatorType = DiscriminatorType.STRING)
abstract class ResourceVersion : Entity() {
    @get:OneToOne
    @get:JoinColumn(name = "resource_node", referencedColumnName = "id")
    var resource: Resource? by changeSupport()

    @get:OneToOne
    @get:JoinColumn(name = "parent", referencedColumnName = "id")
    var parent: Resource? by changeSupport()

    @get:OneToOne
    @get:JoinColumn(name = "space_id", referencedColumnName = "id")
    var space: Space? by changeSupport()

    @get:OneToOne
    @get:JoinColumn(name = "revision_from_id", referencedColumnName = "id")
    var revision: Revision? by changeSupport()

    @get:Column(name = "name")
    var name: String by changeSupport()

    @get:Column(name = "revision_from_time")
    var revisionFrom: Instant by changeSupport()

    @get:Column(name = "revision_to_time")
    var revisionTo: Instant by changeSupport(MAX_DB_TIMESTAMP)

    @get:Column(name = "entity_version_id")
    var versionId: String by changeSupport()

    @get:Column(name = "deleted")
    var deleted: Boolean by changeSupport(false)

    @Suppress("JpaAttributeTypeInspection")
    abstract fun getResourceVersionType(): ResourceType
}