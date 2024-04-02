package org.mariella.persistence.runtime;

public interface ModificationTrackerParticipantsListener {

    void addedNewParticipant(Object participant);

    void addedExistingParticipant(Object participant);

    void removedParticipant(Object participand);

}
