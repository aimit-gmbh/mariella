package org.mariella.test.model;

import org.mariella.persistence.runtime.TrackedList;

import javax.persistence.*;
import java.sql.Timestamp;

@javax.persistence.Entity
@Table(name = "RESOURCE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "TYPE",
        discriminatorType = DiscriminatorType.STRING
)
public class Resource extends Entity {
    private String name;
    private Timestamp lastModified;
    private Folder parent;
    private TrackedList<Resource> children = new TrackedList<>(propertyChangeSupport, "children");

    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        propertyChangeSupport.firePropertyChange("name", this.name, this.name = name);
    }

    @ManyToOne
    @JoinColumn(name = "PARENT_ID", referencedColumnName = "ID")
    public Folder getParent() {
        return parent;
    }

    public void setParent(Folder parent) {
        propertyChangeSupport.firePropertyChange("parent", this.parent, this.parent = parent);
    }

    @OneToMany(mappedBy = "parent")
    public TrackedList<Resource> getChildren() {
        return children;
    }

    @Column(name = "LAST_MODIFIED")
    public Timestamp getLastModified() {
        return lastModified;
    }

    public void setLastModified(Timestamp lastModified) {
        propertyChangeSupport.firePropertyChange("lastModified", this.lastModified, this.lastModified = lastModified);
    }

}
