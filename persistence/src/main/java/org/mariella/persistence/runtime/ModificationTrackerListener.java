package org.mariella.persistence.runtime;

public interface ModificationTrackerListener {

    void propertyChanged(Object modifiable, String propertyName, Object oldValue, Object newValue);

    void indexedPropertyChanged(Object modifiable, String propertyName, int index, Object oldValue, Object newValue);
}
