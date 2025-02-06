package org.mariella.test.model;

import java.util.List;

import javax.persistence.*;

import org.mariella.persistence.runtime.TrackedList;

@javax.persistence.Entity
@Table(name = "COMPANY")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue("C")
@PrimaryKeyJoinColumn(name = "ID", referencedColumnName = "ID")
public class Company extends Partner {
    private String name;
    private Person boss;

    private List<Employment> employments = new TrackedList<>(propertyChangeSupport, "employments");
    
    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        propertyChangeSupport.firePropertyChange("name", this.name, this.name = name);
    }

    @OneToOne
    @JoinColumn(name = "boss_id", referencedColumnName = "id")
    public Person getBoss() {
        return boss;
    }

    public void setBoss(Person boss) {
        propertyChangeSupport.firePropertyChange("boss", this.boss, this.boss = boss);
    }
    
    @OneToMany(mappedBy="employer")
    public List<Employment> getEmployments() {
		return employments;
	}
}
