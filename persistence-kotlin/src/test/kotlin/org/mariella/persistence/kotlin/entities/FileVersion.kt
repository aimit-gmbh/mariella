package org.mariella.persistence.kotlin.entities

import javax.persistence.*
import javax.persistence.Entity

@Entity
@Table(name = "file_version")
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue("FIV")
class FileVersion : ResourceVersion() {
    @get:Column(name = "file_store_path")
    var path: String by changeSupport()

    @get:Column(name = "filesize")
    var size: Long by changeSupport()

    @get:Column(name = "file_hash")
    var hash: ByteArray? by changeSupport()

    @Suppress("JpaAttributeTypeInspection")
    override fun getResourceVersionType(): ResourceType {
        return ResourceType.FileVersion
    }
}