package org.mariella.persistence.kotlin.entities

import org.mariella.persistence.kotlin.StringMappedSealedClass

sealed class UserRole(override val value: String) : StringMappedSealedClass {
    data object Donkey : UserRole("donkey")
    data object CodeMonkey : UserRole("codemonkey")
    data object God : UserRole("god")
}