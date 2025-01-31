package org.mariella.test.model;

import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "COMPANY")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue("C")
@PrimaryKeyJoinColumn(name = "ID", referencedColumnName = "ID")
public class Company extends Partner {
    private String name;
    private Person boss;

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
}
