package org.mariella.test.model;

import org.mariella.persistence.runtime.TrackedList;

import javax.persistence.*;
import java.util.List;

@javax.persistence.Entity
@Table(name = "PARTNER")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(
        name = "TYPE",
        discriminatorType = DiscriminatorType.STRING
)

public class Partner extends Entity {
    private String alias;
    private List<Partner> collaborators = new TrackedList<>(propertyChangeSupport, "collaborators");

    @Column(name = "ALIAS")
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        propertyChangeSupport.firePropertyChange("alias", this.alias, this.alias = alias);
    }

    @ManyToMany
    @JoinTable(
            name = "COLLABORATORS",
            joinColumns = @JoinColumn(name = "PARTNER_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "COLLABORATOR_ID", referencedColumnName = "ID")
    )

    public List<Partner> getCollaborators() {
        return collaborators;
    }

}
