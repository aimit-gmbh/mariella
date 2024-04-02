package org.mariella.persistence.runtime;

import java.beans.PropertyChangeSupport;

public class TrackedList<T> extends ObservableList<T> {
    private final PropertyChangeSupport propertyChangeSupport;

    public TrackedList(PropertyChangeSupport propertyChangeSupport, String propertyName) {
        super(propertyName);
        this.propertyChangeSupport = propertyChangeSupport;
    }

    @Override
    protected void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
        propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

}
