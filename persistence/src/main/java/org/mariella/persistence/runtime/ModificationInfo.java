package org.mariella.persistence.runtime;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ModificationInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
    protected final Object object;
    private Collection<String> modifiedProperties = new HashSet<>();
    private Map<String, CollectionModificationInfo> collectionModificationInfos =
            new HashMap<>();

    private Status status;

    public ModificationInfo(Object object) {
        super();
        this.object = object;
    }

    ModificationInfo getCopy() {
        ModificationInfo copy = new ModificationInfo(object);
        copy.status = status;
        copy.modifiedProperties = new HashSet<>(modifiedProperties);
        for (Map.Entry<String, CollectionModificationInfo> cmi : collectionModificationInfos.entrySet()) {
            copy.collectionModificationInfos.put(cmi.getKey(), cmi.getValue().getCopy());
        }
        return copy;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Object getObject() {
        return object;
    }

    public Collection<String> getModifiedProperties() {
        return modifiedProperties;
    }

    public void setModifiedProperties(Collection<String> modifiedProperties) {
        this.modifiedProperties = modifiedProperties;
    }

    public CollectionModificationInfo getCollectionModificationInfo(String propertyName) {
        CollectionModificationInfo cmi = collectionModificationInfos.get(propertyName);
        if (cmi == null) {
            cmi = new CollectionModificationInfo();
            collectionModificationInfos.put(propertyName, cmi);
        }
        return cmi;
    }

    public Collection<String> getCollectionModifiedProperties() {
        return collectionModificationInfos.keySet();
    }

    public void clearCollectionModificationInfos() {
        collectionModificationInfos = new HashMap<>();
    }

    public void markModified(String propertyName) {
        modifiedProperties.add(propertyName);
    }

    public void unmarkModified(String propertyName) {
        modifiedProperties.remove(propertyName);
    }

    public boolean isModified(String propertyName) {
        return modifiedProperties.contains(propertyName);
    }

    public enum Status {
        Untouched,
        New,
        Modified,
        Removed,
        NewRemoved
    }

}
