package org.mariella.test.entities

import javax.persistence.*
import javax.persistence.Entity

@Entity
@Table(name = "file_node")
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue("FI")
class File : Resource() {
    override fun isContainer(): Boolean {
        return false
    }
}