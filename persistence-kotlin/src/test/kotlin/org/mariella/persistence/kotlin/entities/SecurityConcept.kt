package org.mariella.persistence.kotlin.entities

import org.mariella.persistence.kotlin.IntegerMappedSealedClass

sealed class SecurityConcept(override val value: Int) : IntegerMappedSealedClass {
    data object Public : SecurityConcept(1)
    data object Space : SecurityConcept(2)
    data object Acl : SecurityConcept(3)
}