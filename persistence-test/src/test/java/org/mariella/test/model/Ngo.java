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
@Table(name = "NGO")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue("N")
@PrimaryKeyJoinColumn(name = "PARTNER_ID", referencedColumnName = "ID")
public class Ngo extends Partner {
	private String title;
    private Person head;

    @Column(name = "TITLE")
    public String getTitle() {
		return title;
	}

    public void setTitle(String title) {
        propertyChangeSupport.firePropertyChange("title", this.title, this.title = title);
    }
    @OneToOne
    @JoinColumn(name = "head_id", referencedColumnName = "id")
    public Person getHead() {
        return head;
    }

    public void setHead(Person head) {
        propertyChangeSupport.firePropertyChange("head", this.head, this.head = head);
    }
}
