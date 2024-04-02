package org.mariella.persistence.loader;

import org.mariella.persistence.persistor.ClusterDescription;
import org.mariella.persistence.runtime.CollectionModificationInfo;
import org.mariella.persistence.runtime.ModifiableAccessor;
import org.mariella.persistence.runtime.ModificationInfo;
import org.mariella.persistence.runtime.ModificationTracker;
import org.mariella.persistence.schema.ClassDescription;
import org.mariella.persistence.schema.CollectionPropertyDescription;
import org.mariella.persistence.schema.PropertyDescription;

import java.util.*;

public class JoinedCollectionManager {
    private final Map<Object, Map<CollectionPropertyDescription, Collection<Object>>> joinedCollections =
            new HashMap<>();
    private final ModificationTracker modificationTracker;

    public JoinedCollectionManager(ModificationTracker modificationTracker) {
        super();
        this.modificationTracker = modificationTracker;
    }

    public void newObject(ClusterDescription clusterDescription, String pathExpression,
                          ClassDescription effectiveClassDescription, Object object) {
        Map<CollectionPropertyDescription, Collection<Object>> map = joinedCollections.computeIfAbsent(object,
                k -> new HashMap<>());
        for (String childExpression : clusterDescription.getChildPathExpressions(pathExpression)) {
            int idx = childExpression.indexOf('.');
            String propertyName = idx == -1 ? childExpression : childExpression.substring(idx + 1);
            PropertyDescription pd = effectiveClassDescription.getPropertyDescription(propertyName);
            if (pd instanceof CollectionPropertyDescription && !map.containsKey(pd)) {
                map.put((CollectionPropertyDescription) pd, new HashSet<>());
            }
        }
    }

    public void addedToCollection(Object object, CollectionPropertyDescription pd, Object value) {
        Map<CollectionPropertyDescription, Collection<Object>> map = joinedCollections.get(object);
        map.get(pd).add(value);
    }

    public void finish() {
        for (Map.Entry<Object, Map<CollectionPropertyDescription, Collection<Object>>> entry : joinedCollections.entrySet()) {
            for (Map.Entry<CollectionPropertyDescription, Collection<Object>> entry2 : entry.getValue().entrySet()) {
                finish(entry.getKey(), entry2.getKey(), entry2.getValue());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void finish(Object object, CollectionPropertyDescription pd, Collection<Object> added) {
        List<Object> value = (List<Object>) ModifiableAccessor.Singleton.getValue(object, pd);
        ModificationInfo mi = modificationTracker.getModificationInfo(object);
        CollectionModificationInfo cmi = null;
        if (mi != null) {
            cmi = mi.getCollectionModificationInfo(pd.getPropertyDescriptor().getName());
        }

        Iterator<?> i = value.iterator();
        while (i.hasNext()) {
            Object entry = i.next();
            if (!added.contains(entry)) {
                if (cmi == null || !cmi.getRemoved().contains(entry)) {
                    i.remove();
                }
            }
        }

        for (Object o : added) {
            if (!value.contains(o)) {
                value.add(o);
            }
        }
    }

}
