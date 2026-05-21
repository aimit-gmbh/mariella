package org.mariella.persistence.kotlin.entities

import jakarta.persistence.*
import jakarta.persistence.Entity


@Entity
@Table(name = "dpurge_version")
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue("PUV")
class PurgeVersion : ResourceVersion() {

    override fun getResourceVersionType(): ResourceType {
        return ResourceType.PurgeVersion
    }
}