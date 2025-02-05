package org.mariella.persistence.kotlin.entities

import org.mariella.persistence.kotlin.StringMappedSealedClass

sealed class ResourceType(override val value: String) : StringMappedSealedClass {
    data object File : ResourceType("FI")
    data object FileVersion : ResourceType("FIV")
    data object Folder : ResourceType("FO")
    data object FolderVersion : ResourceType("FOV")
    data object Project : ResourceType("PR")
    data object ProjectVersion : ResourceType("PRV")
    data object Metadata : ResourceType("MD")
}