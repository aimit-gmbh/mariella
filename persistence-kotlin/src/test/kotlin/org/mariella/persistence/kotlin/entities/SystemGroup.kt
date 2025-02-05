package org.mariella.persistence.kotlin.entities

import org.mariella.persistence.kotlin.IntegerMappedSealedClass

sealed class SystemGroup(override val value: Int) : IntegerMappedSealedClass {
    data object None : SystemGroup(1)
    data object SuperUser : SystemGroup(2)
    data object Admin : SystemGroup(3)
}