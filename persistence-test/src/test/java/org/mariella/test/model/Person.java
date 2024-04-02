package org.mariella.test.model;

import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "PERSON")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue("P")
@PrimaryKeyJoinColumn(name = "ID", referencedColumnName = "ID")
public class Person extends Partner {
    private String firstName;
    private String lastName;

    @Column(name = "FIRST_NAME")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        propertyChangeSupport.firePropertyChange("firstName", this.firstName, this.firstName = firstName);
    }

    @Column(name = "LAST_NAME")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        propertyChangeSupport.firePropertyChange("lastName", this.lastName, this.lastName = lastName);
    }

}
