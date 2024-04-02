package org.mariella.persistence.runtime;

public interface ModificationTrackerEntityListener {

    void participantAdded(Object participant, ModificationInfo info);

    void participantRemoved(Object participant, ModificationInfo info);

    void participantFlushed(Object participant, ModificationInfo info);

}
