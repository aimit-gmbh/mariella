package org.mariella.test.model;

import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "COMPANY")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue("C")
@PrimaryKeyJoinColumn(name = "ID", referencedColumnName = "ID")
public class Company extends Partner {
    private String name;

    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        propertyChangeSupport.firePropertyChange("name", this.name, this.name = name);
    }

}
