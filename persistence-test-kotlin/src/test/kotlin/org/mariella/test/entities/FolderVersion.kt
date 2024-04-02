package org.mariella.test.entities

import javax.persistence.*
import javax.persistence.Entity

@Entity
@Table(name = "folder_version")
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue("FOV")
class FolderVersion : ResourceVersion() {

    override fun getResourceVersionType(): ResourceType {
        return ResourceType.FolderVersion
    }

}