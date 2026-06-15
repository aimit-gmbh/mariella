package org.mariella.test.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@jakarta.persistence.Entity
@Table(name = "COMPANY")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue("C")
@PrimaryKeyJoinColumn(name = "PARTNER_ID", referencedColumnName = "ID")
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
