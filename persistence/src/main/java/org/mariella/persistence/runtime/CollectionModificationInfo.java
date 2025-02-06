package org.mariella.persistence.runtime;

import java.beans.IndexedPropertyChangeEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class CollectionModificationInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
    private Collection<Object> added = null;
    private Collection<Object> removed = null;

    CollectionModificationInfo getCopy() {
        CollectionModificationInfo copy = new CollectionModificationInfo();
        copy.added = (added == null ? null : new ArrayList<>(added));
        copy.removed = (removed == null ? null : new ArrayList<>(removed));
        return copy;
    }

    public boolean hasChanges() {
        return added != null && !added.isEmpty() || removed != null && !removed.isEmpty();
    }

    public Collection<Object> getAdded() {
        if (added == null) {
            added = new ArrayList<>();
        }
        return added;
    }

    public Collection<Object> getRemoved() {
        if (removed == null) {
            removed = new ArrayList<>();
        }
        return removed;
    }

    public void changed(IndexedPropertyChangeEvent event) {
        if (event.getOldValue() == null && event.getNewValue() != null) {
            added(event.getNewValue());
        } else if (event.getOldValue() != null) {
            removed(event.getOldValue());
            if (event.getNewValue() != null) {
                added(event.getNewValue());
            }
        }
    }

    private void added(Object object) {
        if (getRemoved().contains(object)) {
            getRemoved().remove(object);
        } else {
            getAdded().add(object);
        }
    }

    private void removed(Object object) {
        if (getAdded().contains(object)) {
            getAdded().remove(object);
        } else {
            getRemoved().add(object);
        }
    }

}
