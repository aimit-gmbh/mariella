package org.mariella.persistence.runtime;

import org.mariella.persistence.schema.ClassDescription;
import org.mariella.persistence.schema.SchemaDescription;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;


public abstract class AbstractModificationTrackerImpl implements PropertyChangeListener, ModificationTracker, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final List<ModificationTrackerListener> persistentListeners = new ArrayList<>();
    private final List<ModificationTrackerEntityListener> entityListeners = new ArrayList<>();
    protected Map<Object, Object> participants = new HashMap<>();
    protected List<ModificationInfo> modifications = new ArrayList<>();
    protected Map<Object, ModificationInfo> modificationMap = new HashMap<>();
    private transient List<ModificationTrackerListener> listeners;
    private SerializableList<ModificationTrackerParticipantsListener> participantsListeners;

    private boolean enabled = true;

    private SavePointSupport savePointSupport = null;

    public AbstractModificationTrackerImpl() {
        super();
    }

    public abstract SchemaDescription getSchemaDescription();

    @Override
    public Object getIdentity(Object participant) {
        return getSchemaDescription().getClassDescription(participant.getClass().getName()).getIdentity(participant);
    }

    protected List<ModificationTrackerListener> getListeners() {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        return listeners;
    }

    protected List<ModificationTrackerParticipantsListener> getParticipantsListeners() {
        if (participantsListeners == null) {
            participantsListeners = new SerializableList<>();
        }
        return participantsListeners;
    }

    @Override
    public void addListener(ModificationTrackerListener listener) {
        getListeners().add(listener);
    }

    @Override
    public void addParticipantsListener(ModificationTrackerParticipantsListener listener) {
        getParticipantsListeners().add(listener);
    }

    @Override
    public void removeListener(ModificationTrackerListener listener) {
        getListeners().remove(listener);
    }

    @Override
    public void removeParticipantsListener(ModificationTrackerParticipantsListener listener) {
        getParticipantsListeners().remove(listener);
    }

    protected List<ModificationTrackerEntityListener> getEntityListeners() {
        return entityListeners;
    }

    public void addEntityListener(ModificationTrackerEntityListener listener) {
        getEntityListeners().add(listener);
    }

    public void removeEntityListener(ModificationTrackerEntityListener listener) {
        getEntityListeners().remove(listener);
    }

    protected List<ModificationTrackerListener> getPersistentListeners() {
        return persistentListeners;
    }

    @Override
    public void addPersistentListener(ModificationTrackerListener listener) {
        getPersistentListeners().add(listener);
    }

    @Override
    public void removePersistentListener(ModificationTrackerListener listener) {
        getPersistentListeners().remove(listener);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void addNewParticipant(Object participant) {
        ModificationInfo info = new ModificationInfo(participant);
        info.setStatus(ModificationInfo.Status.New);
        addModificationInfo(info);
        PropertyChangeUtils.addPropertyChangeListener(participant, this);

        for (ModificationTrackerEntityListener entityListener : getEntityListeners()) {
            entityListener.participantAdded(participant, info);
        }
        for (ModificationTrackerParticipantsListener listener : getParticipantsListeners()) {
            listener.addedNewParticipant(participant);
        }
    }

    @Override
    public void addExistingParticipant(Object participant) {
        primitiveAddExistingParticipant(participant);
        PropertyChangeUtils.addPropertyChangeListener(participant, this);
        for (ModificationTrackerParticipantsListener listener : getParticipantsListeners()) {
            listener.addedNewParticipant(participant);
        }
    }

    public void primitiveAddExistingParticipant(Object participant) {
        Object identity = getIdentity(participant);
        participants.put(identity, participant);
        for (ModificationTrackerEntityListener entityListener : getEntityListeners()) {
            entityListener.participantAdded(participant, null);
        }
    }

    public boolean isParticipant(Object participant) {
        Object identity = getIdentity(participant);
        return participants.containsKey(identity);

    }

    public void primitiveRemoveExistingParticipant(Object participant) {
        Object identity = getIdentity(participant);
        if (identity != null) {
            participants.remove(identity);
        }
        ModificationInfo modificationInfo = modificationMap.get(participant);
        if (modificationInfo != null) {
            modificationMap.remove(participant);
            modifications.remove(modificationInfo);
        }
        PropertyChangeUtils.removePropertyChangeListener(participant, this);
        for (ModificationTrackerEntityListener entityListener : getEntityListeners()) {
            entityListener.participantRemoved(participant, null);
        }
    }

    @Override
    public Object getParticipant(Object identity) {
        return participants.get(identity);
    }

    @Override
    public boolean isDirty() {
        return !modifications.isEmpty();
    }

    @Override
    public boolean isDirty(Object persistable) {
        return modificationMap.containsKey(persistable);
    }

    @Override
    public void remove(Object modifiable) {
        ModificationInfo info = modificationMap.get(modifiable);
        if (info == null) {
            info = new ModificationInfo(modifiable);
            info.setStatus(getIdentity(modifiable) == null ? ModificationInfo.Status.NewRemoved : ModificationInfo.Status.Removed);
            addModificationInfo(info);
        } else {
            if (info.getStatus() == ModificationInfo.Status.New) {
                info.setStatus(ModificationInfo.Status.NewRemoved);
            } else {
                info.setStatus(ModificationInfo.Status.Removed);
            }
        }
        for (ModificationTrackerParticipantsListener listener : getParticipantsListeners()) {
            listener.removedParticipant(modifiable);
        }
    }

    @Override
    public void flushed() {
        if (savePointSupport != null) {
            savePointSupport.deleteAllSavePoints();
        }
        for (ModificationInfo info : modifications) {
            if (info.getStatus() == ModificationInfo.Status.Removed || info.getStatus() == ModificationInfo.Status.NewRemoved) {
                participants.remove(info.getObject());
                for (ModificationTrackerEntityListener entityListener : getEntityListeners()) {
                    entityListener.participantRemoved(info.getObject(), info);
                }
            } else {
                if (info.getStatus() == ModificationInfo.Status.New) {
                    participants.put(getIdentity(info.getObject()), info.getObject());
                }
                for (ModificationTrackerEntityListener entityListener : getEntityListeners()) {
                    entityListener.participantFlushed(info.getObject(), info);
                }
            }
        }
        modifications = new ArrayList<>();
        modificationMap = new HashMap<>();
    }

    public void propertyChange(PropertyChangeEvent event) {
        if (enabled) {
            Object participant = event.getSource();
            ClassDescription cd = getSchemaDescription().getClassDescription(participant.getClass().getName());
            if (cd != null && cd.getPropertyDescription(event.getPropertyName()) != null) {
                ModificationInfo info = modificationMap.get(participant);
                if (info == null) {
                    info = new ModificationInfo(participant);
                    info.setStatus(ModificationInfo.Status.Modified);
                    addModificationInfo(info);
                }
                info.markModified(event.getPropertyName());
                if (event instanceof IndexedPropertyChangeEvent) {
                    CollectionModificationInfo cmi = info.getCollectionModificationInfo(event.getPropertyName());
                    cmi.changed((IndexedPropertyChangeEvent) event);
                    for (ModificationTrackerListener listener : getPersistentListeners()) {
                        listener.indexedPropertyChanged(participant, event.getPropertyName(), ((IndexedPropertyChangeEvent) event).getIndex(), event.getOldValue(), event.getNewValue());
                    }
                    for (ModificationTrackerListener listener : getListeners()) {
                        listener.indexedPropertyChanged(participant, event.getPropertyName(), ((IndexedPropertyChangeEvent) event).getIndex(), event.getOldValue(), event.getNewValue());
                    }
                } else {
                    for (ModificationTrackerListener listener : getPersistentListeners()) {
                        listener.propertyChanged(participant, event.getPropertyName(), event.getOldValue(), event.getNewValue());
                    }
                    for (ModificationTrackerListener listener : getListeners()) {
                        listener.propertyChanged(participant, event.getPropertyName(), event.getOldValue(), event.getNewValue());
                    }
                }
            }
        }
    }

    @Override
    public void detachAll() {
        for (Object participant : participants.values()) {
            PropertyChangeUtils.removePropertyChangeListener(participant, this);
        }
        participants.clear();
        modifications.clear();
        modificationMap.clear();
    }

    protected void detach(Object participant) {
        boolean removed = false;
        PropertyChangeUtils.removePropertyChangeListener(participant, this);
        ModificationInfo mi = modificationMap.remove(participant);
        if (mi != null) {
            removed = modifications.remove(mi);
        }
        removed |= participants.values().remove(participant);
        if (removed) {
            for (ModificationTrackerParticipantsListener listener : getParticipantsListeners()) {
                listener.removedParticipant(participant);
            }
        }
    }

    @Override
    public void dispose() {
        for (Object participant : participants.values()) {
            PropertyChangeUtils.removePropertyChangeListener(participant, this);
        }
        participants = null;
        modifications = null;
        modificationMap = null;
    }

    public void addModificationInfo(ModificationInfo modificationInfo) {
        if (modificationMap.containsKey(modificationInfo.getObject())) {
            throw new IllegalArgumentException();
        }

        modifications.add(modificationInfo);
        modificationMap.put(modificationInfo.getObject(), modificationInfo);
//	participants.values().remove(modificationInfo.getObject());
    }


    public void removeModificationInfo(ModificationInfo info) {
        modificationMap.remove(info.getObject());
        modifications.remove(info);
    }


    @Override
    public ModificationInfo getModificationInfo(Object modifiable) {
        return modificationMap.get(modifiable);
    }

    @Override
    public List<ModificationInfo> getModifications() {
        return modifications;
    }

    @Override
    public boolean moveBefore(Object toBeMoved, Object target) {
        SourceAndTargets result = getSourceAndTargets(toBeMoved, target);
        if (result == null) return false;
        if (result.iToBeMoved() > result.iTarget()) {
            modifications.remove(result.iToBeMoved());
            modifications.add(result.iTarget(), result.miToBeMoved());
            return true;
        } else {
            return false;
        }
    }

    private SourceAndTargets getSourceAndTargets(Object toBeMoved, Object target) {
        ModificationInfo miToBeMoved = getModificationInfo(toBeMoved);
        ModificationInfo miTarget = getModificationInfo(target);

        if (miToBeMoved == null || miTarget == null) {
            return null;
        }
        int iToBeMoved = modifications.indexOf(miToBeMoved);
        int iTarget = modifications.indexOf(miTarget);
        return new SourceAndTargets(miToBeMoved, iToBeMoved, iTarget);
    }

    @Override
    public boolean moveAfter(Object toBeMoved, Object target) {
        SourceAndTargets result = getSourceAndTargets(toBeMoved, target);
        if (result == null) return false;
        if (result.iToBeMoved < result.iTarget) {
            modifications.remove(result.iToBeMoved);
            if (modifications.size() == result.iTarget) {
                modifications.add(result.miToBeMoved);
            } else {
                modifications.add(result.iTarget + 1, result.miToBeMoved);
            }
            return true;
        } else {
            return false;
        }
    }

    public Collection<?> getParticipants() {
        return participants.values();
    }

    IModificationTrackerMemento createMemento() {
        return new IModificationTrackerMemento() {
            private final Map<Object, Object> participants;
            private final List<ModificationInfo> modifications;

            {
                participants = new HashMap<>(AbstractModificationTrackerImpl.this.participants);
                modifications = new ArrayList<>(AbstractModificationTrackerImpl.this.modifications.size());
                for (ModificationInfo mi : AbstractModificationTrackerImpl.this.modifications) {
                    modifications.add(mi.getCopy());
                }
            }

            @Override
            public void restore() {
                AbstractModificationTrackerImpl.this.participants = participants;
                AbstractModificationTrackerImpl.this.modifications = modifications;
                modificationMap.clear();
                for (ModificationInfo mi : modifications) {
                    modificationMap.put(mi.getObject(), mi);
                }
            }
        };
    }

    SavePointSupport getSavePointSupport() {
        if (savePointSupport == null) {
            savePointSupport = new SavePointSupport(this);
        }
        return savePointSupport;
    }

    interface IModificationTrackerMemento {
        void restore();
    }

    private record SourceAndTargets(ModificationInfo miToBeMoved, int iToBeMoved, int iTarget) {
    }


}
