package org.mariella.persistence.runtime;

import org.mariella.persistence.runtime.AbstractModificationTrackerImpl.IModificationTrackerMemento;

import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

class SavePointSupport {

    private final AbstractModificationTrackerImpl modificationTracker;
    private boolean propertyListenerHooked = false;
    private List<SavePoint> savePoints = new ArrayList<>();
    private ArrayList<Modification> modifications = new ArrayList<>(30);
    private final ModificationTrackerListener propertyListener = new ModificationTrackerListener() {
        @Override
        public void propertyChanged(Object modifiable, String propertyName, Object oldValue, Object newValue) {
            modifications.add(new PropertyChangeModification(modifiable, propertyName, oldValue, newValue));
        }

        @Override
        public void indexedPropertyChanged(Object modifiable, String propertyName, int index, Object oldValue, Object newValue) {
            if (index < 0) {
                throw new UnsupportedOperationException("Indexed property change with index " + index + " not allowed!");
            }
            modifications.add(new IndexedPropertyChangeModification(modifiable, propertyName, index, oldValue, newValue));
        }
    };

    SavePointSupport(AbstractModificationTrackerImpl modificationTracker) {
        this.modificationTracker = modificationTracker;
    }

    private void unkookPropertyTracker() {
        if (propertyListenerHooked) {
            modificationTracker.removeListener(propertyListener);
            propertyListenerHooked = false;
        }
    }

    private void updatePropertyTracker() {
        if (propertyListenerHooked && savePoints.isEmpty()) {
            modificationTracker.removeListener(propertyListener);
            propertyListenerHooked = false;
        } else if (!propertyListenerHooked && !savePoints.isEmpty()) {
            modificationTracker.addListener(propertyListener);
            propertyListenerHooked = true;
        }
    }

    SavePoint createSavePoint() {
        SavePoint savePoint = new SavePoint(this, modifications.size());
        savePoints.add(savePoint);
        modifications.add(new ModificationTrackerMemento(modificationTracker));
        updatePropertyTracker();
        return savePoint;
    }

    void deleteToSavePoint(SavePoint savePoint) {
        int savePointIndex = savePoints.indexOf(savePoint);
        if (savePointIndex >= 0) {
            unkookPropertyTracker();
            while (modifications.size() > savePoint.getSaveIndex()) {
                modifications.remove(modifications.size() - 1);
            }
            while (savePoints.size() > savePointIndex) {
                savePoints.remove(savePoints.size() - 1);
            }
            updatePropertyTracker();
        } else {
            throw new RuntimeException("Error deleting save point: Unknown or deleted save point!");
        }
    }

    void deleteAllSavePoints() {
        modifications = new ArrayList<>(30);
        savePoints = new ArrayList<>();
    }

    void rollbackToSavePoint(SavePoint savePoint) {
        int savePointIndex = savePoints.indexOf(savePoint);
        if (savePointIndex >= 0) {
            Map<Class<?>, BeanAccessor<Object>> accessorMap = new HashMap<>();
            unkookPropertyTracker();
            List<ModificationTrackerListener> persistentListeners = modificationTracker.getPersistentListeners();
            List<ModificationTrackerListener> pl = new ArrayList<>(persistentListeners);
            persistentListeners.clear();
            while (modifications.size() > savePoint.getSaveIndex()) {
                modifications.remove(modifications.size() - 1).undo(accessorMap);
            }
            while (savePoints.size() > savePointIndex) {
                savePoints.remove(savePoints.size() - 1);
            }
            modificationTracker.getPersistentListeners().addAll(pl);
            updatePropertyTracker();
        } else {
            throw new RuntimeException("Error rolling back save point: Unknown or deleted save point!");
        }
    }

    private interface Modification {
        void undo(Map<Class<?>, BeanAccessor<Object>> accessorMap);
    }

    private static class BeanAccessor<T> {
        private final Class<T> beanClass;
        private final PropertyDescriptor[] properties;

        public BeanAccessor(Class<T> beanClass) {
            this.beanClass = beanClass;
            try {
                properties = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
                Arrays.sort(properties, Comparator.comparing(FeatureDescriptor::getName));
            } catch (IntrospectionException ex) {
                throw new RuntimeException("Error introspecting class " + beanClass.getName() + ".", ex);
            }
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        public static BeanAccessor<Object> beanAccessor(Map<Class<?>, BeanAccessor<Object>> accessorMap, Object object) {
            Class<?> clazz = object.getClass();
            BeanAccessor<Object> accessor = accessorMap.get(clazz);
            if (accessor == null) {
                accessor = new BeanAccessor<Object>((Class) clazz);
                accessorMap.put(clazz, accessor);
            }
            return accessor;
        }

        public PropertyDescriptor getPropertyDescriptor(String propertyName) {
            int index = Arrays.binarySearch(properties, propertyName, (o1, o2) -> {
                String pn1 = (o1 instanceof PropertyDescriptor ? ((PropertyDescriptor) o1).getName() : (String) o1);
                String pn2 = (o2 instanceof PropertyDescriptor ? ((PropertyDescriptor) o2).getName() : (String) o2);
                return pn1.compareTo(pn2);
            });
            if (index < 0) {
                throw new RuntimeException("Unknown property '" + propertyName + "' of class " + beanClass.getName() + ".");
            } else {
                return properties[index];
            }
        }

        public Object getValue(T object, String propertyName) {
            try {
                return getPropertyDescriptor(propertyName).getReadMethod().invoke(object);
            } catch (InvocationTargetException ex) {
                Throwables.throwIfUnchecked(ex.getCause());
                throw new RuntimeException("Error reading property '" + propertyName + "' of class " + beanClass.getName() + ".",
                        ex.getCause());
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Error reading property '" + propertyName + "' of class " + beanClass.getName() + ".",
                        ex);
            }
        }

        public void setValue(T object, String propertyName, Object value) {
            try {
                getPropertyDescriptor(propertyName).getWriteMethod().invoke(object, value);
            } catch (InvocationTargetException ex) {
                Throwables.throwIfUnchecked(ex.getCause());
                throw new RuntimeException("Error setting property '" + propertyName + "' of class " + beanClass.getName() + ".",
                        ex.getCause());
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Error setting property '" + propertyName + "' of class " + beanClass.getName() + ".",
                        ex);
            }
        }
    }

    private static class PropertyChangeModification implements Modification {
        private final Object object;
        private final String propertyName;
        private final Object oldValue;
        // private Object newValue; // for redo, if someone wants to implement

        private PropertyChangeModification(Object object, String propertyName, Object oldValue, Object ignoredNewValue) {
            this.object = object;
            this.propertyName = propertyName;
            this.oldValue = oldValue;
            // this.newValue = newValue;
        }

        @Override
        public void undo(Map<Class<?>, BeanAccessor<Object>> accessorMap) {
            BeanAccessor<Object> accessor = BeanAccessor.beanAccessor(accessorMap, object);
            accessor.setValue(object, propertyName, oldValue);
        }
    }

    private record IndexedPropertyChangeModification(Object object, String propertyName, int index, Object oldValue,
                                                     Object newValue) implements Modification {

        @SuppressWarnings({"unchecked"})
        @Override
        public void undo(Map<Class<?>, BeanAccessor<Object>> accessorMap) {
            BeanAccessor<Object> accessor = BeanAccessor.beanAccessor(accessorMap, object);
            List<Object> list = (List<Object>) accessor.getValue(object, propertyName);
            if (oldValue == null) {
                // undo add
                list.remove(index);
            } else if (newValue == null) {
                // undo remove
                list.add(index, oldValue);
            } else {
                // undo set
                list.set(index, oldValue);
            }
        }
    }

    private static class ModificationTrackerMemento implements Modification {
        private final IModificationTrackerMemento modificationTrackerMemento;

        private ModificationTrackerMemento(AbstractModificationTrackerImpl tracker) {
            modificationTrackerMemento = tracker.createMemento();
        }

        @Override
        public void undo(Map<Class<?>, BeanAccessor<Object>> accessorMap) {
            modificationTrackerMemento.restore();
        }
    }

}
