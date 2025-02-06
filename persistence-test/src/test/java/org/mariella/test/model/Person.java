package org.mariella.test.model;

import java.util.List;

import javax.persistence.*;

import org.mariella.persistence.runtime.TrackedList;

@javax.persistence.Entity
@Table(name = "PERSON")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue("P")
@PrimaryKeyJoinColumn(name = "ID", referencedColumnName = "ID")
public class Person extends Partner {
    private String firstName;
    private String lastName;

    private List<Employment> employments = new TrackedList<>(propertyChangeSupport, "employments");

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

    @OneToMany(mappedBy="employee")
    public List<Employment> getEmployments() {
		return employments;
	}
}
