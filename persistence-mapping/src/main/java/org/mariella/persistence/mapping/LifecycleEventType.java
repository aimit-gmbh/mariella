package org.mariella.persistence.mapping;

public enum LifecycleEventType {
    PrePersist(),
    PostPersist(),
    PreRemove(),
    PostRemove(),
    PreUpdate(),
    PostUpdate(),
    PostLoad()

}
