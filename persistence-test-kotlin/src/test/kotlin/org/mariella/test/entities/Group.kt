package org.mariella.test.entities

import org.mariella.persistence.runtime.TrackedList
import javax.persistence.*
import javax.persistence.Entity


@Entity
@Table(name = "auth_group")
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue("G")
class Group : Member() {

    @get:Column(name = "gname")
    var name: String by changeSupport()

    @get:Column(name = "system_group")
    var systemGroup: SystemGroup by changeSupport(SystemGroup.None)

    @ManyToMany
    @JoinTable(
        name = "auth_membership",
        joinColumns = [JoinColumn(name = "parent_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "child_id", referencedColumnName = "id")]
    )
    val members: MutableList<Member> = TrackedList(propertyChangeSupport, "members")
}