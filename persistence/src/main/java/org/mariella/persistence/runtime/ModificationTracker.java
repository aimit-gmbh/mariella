package org.mariella.persistence.runtime;

import org.mariella.persistence.schema.SchemaDescription;

import java.util.Collection;
import java.util.List;

public interface ModificationTracker {

    SchemaDescription getSchemaDescription();

    Object getIdentity(Object participant);

    void addListener(ModificationTrackerListener listener);

    void addParticipantsListener(ModificationTrackerParticipantsListener listener);

    void removeListener(ModificationTrackerListener listener);

    void removeParticipantsListener(ModificationTrackerParticipantsListener listener);

    void addPersistentListener(ModificationTrackerListener listener);

    void removePersistentListener(ModificationTrackerListener listener);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    void addNewParticipant(Object participant);

    void addExistingParticipant(Object participant);

    Object getParticipant(Object identity);

    boolean isDirty();

    boolean isDirty(Object persistable);

    void remove(Object modifiable);

    void flushed();

    void dispose();

    ModificationInfo getModificationInfo(Object modifiable);

    List<ModificationInfo> getModifications();

    boolean moveBefore(Object toBeMoved, Object target);

    boolean moveAfter(Object toBeMoved, Object target);

    Collection<?> getParticipants();

    void detachAll();
}