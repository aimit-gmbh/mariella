package org.mariella.persistence.kotlin.entities

import jakarta.persistence.*
import jakarta.persistence.Entity

@Entity
@Table(name = "auth_user")
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue("U")
class UserEntity : Member() {
    @get:Column(name = "name")
    var name: String by changeSupport()

    @get:Column(name = "email")
    var email: String by changeSupport()
}