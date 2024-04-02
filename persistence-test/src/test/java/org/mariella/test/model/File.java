package org.mariella.test.model;

import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "RESOURCE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("File")
public class File extends Resource {
    private long size;

    @Column(name = "SIZE")
    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        propertyChangeSupport.firePropertyChange("size", this.size, this.size = size);
    }

}
