package org.mariella.persistence.kotlin.entities

import jakarta.persistence.*
import jakarta.persistence.Entity

@Entity
@Table(name = "dpurge")
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue("PU")
class Purge : Resource() {
    override fun isContainer(): Boolean {
        return false
    }

    @get:ManyToOne
    @get:JoinColumn(name = "requestor", referencedColumnName = "id")
    var requestor: Member? by changeSupport()
}