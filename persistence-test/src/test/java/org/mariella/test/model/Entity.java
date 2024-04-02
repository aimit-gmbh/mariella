package org.mariella.test.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.UUID;

@MappedSuperclass
public class Entity {
    @Transient
    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private UUID id;

    public Entity() {
        this(UUID.randomUUID());
    }

    public Entity(UUID id) {
        this.id = id;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    @Id
    @Column(name = "ID")
//	@GeneratedValue(strategy=GenerationType.AUTO)
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
    }

}
