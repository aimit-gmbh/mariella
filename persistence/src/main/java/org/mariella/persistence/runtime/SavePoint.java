package org.mariella.persistence.runtime;


public class SavePoint {

    private final SavePointSupport undoSupport;
    private final int saveIndex;

    SavePoint(SavePointSupport undoSupport, int saveIndex) {
        this.undoSupport = undoSupport;
        this.saveIndex = saveIndex;
    }

    public static SavePoint create(ModificationTracker modificationTracker) {
        if (modificationTracker instanceof AbstractModificationTrackerImpl) {
            return ((AbstractModificationTrackerImpl) modificationTracker).getSavePointSupport().createSavePoint();
        } else {
            throw new RuntimeException(
                    "SavePoints are only supported for modification trackers of type AbstractModificationTrackerImpl");
        }
    }

    int getSaveIndex() {
        return saveIndex;
    }

    public void delete() {
        undoSupport.deleteToSavePoint(this);
    }

    public void rollback() {
        undoSupport.rollbackToSavePoint(this);
    }

}
