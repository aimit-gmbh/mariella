package org.mariella.persistence.kotlin.entities

import jakarta.persistence.*
import jakarta.persistence.Entity

@Entity
@Table(name = "folder")
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue("FO")
class Folder : Resource() {
    override fun isContainer(): Boolean {
        return true
    }
}