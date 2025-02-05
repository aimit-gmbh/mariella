package org.mariella.persistence.schema;

import org.mariella.persistence.runtime.BeanInfo;
import org.mariella.persistence.runtime.ModifiableAccessor;
import org.mariella.persistence.util.Assert;
import org.mariella.persistence.util.InitializationHelper;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ClassDescription implements Serializable {
    public static final String TYPE_PROPERTY = "org.mariella.persistence.type";
    private final SchemaDescription schemaDescription;
    private final String className;
    private final ClassDescription superClassDescription;
    private final Map<String, PropertyDescription> propertyDescriptions = new HashMap<>();
    private final Map<String, PropertyDescription> hierarchyPropertyDescriptions = new HashMap<>();
    private final Collection<ClassDescription> immediateChildren = new HashSet<>();

    private boolean isAbstract = false;
    private PropertyDescription[] identityPropertyDescriptions;

    private transient Class<?> identityClass;
    private String identityClassName;

    public ClassDescription(SchemaDescription schemaDescription, String className) {
        super();
        this.schemaDescription = schemaDescription;
        this.className = className;
        superClassDescription = null;
    }

    public ClassDescription(SchemaDescription schemaDescription, ClassDescription superClassDescription, String className) {
        super();
        this.schemaDescription = schemaDescription;
        this.className = className;
        this.superClassDescription = superClassDescription;
    }

    public void initialize(InitializationHelper<ClassDescription> context) {
        if (superClassDescription != null) {
            context.ensureInitialized(superClassDescription);
            identityPropertyDescriptions = superClassDescription.getIdentityPropertyDescriptions();
            for (PropertyDescription pd : superClassDescription.propertyDescriptions.values()) {
                propertyDescriptions.put(pd.getPropertyDescriptor().getName(), pd);
            }
            superClassDescription.immediateChildren.add(this);
        }
    }

    public void afterDeserialization(ClassLoader classLoader) {
        if (identityClassName != null) {
            try {
                identityClass = classLoader.loadClass(identityClassName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            Class<?> cls = classLoader.loadClass(className);
            BeanInfo bi = new BeanInfo(cls);
            for (PropertyDescription pd : getPropertyDescriptions()) {
                pd.afterDeserialization(bi);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void postInitialize(InitializationHelper<ClassDescription> context) {
        for (PropertyDescription propertyDescription : propertyDescriptions.values()) {
            hierarchyPropertyDescriptions.put(propertyDescription.getPropertyDescriptor().getName(), propertyDescription);
        }

        for (ClassDescription child : immediateChildren) {
            context.ensureInitialized(child);
            Collection<String> ambigous = new HashSet<>();
            for (PropertyDescription propertyDescription : child.getHierarchyPropertyDescriptions()) {
                if (!ambigous.contains(propertyDescription.getPropertyDescriptor().getName())) {
                    PropertyDescription existing = hierarchyPropertyDescriptions.get(
                            propertyDescription.getPropertyDescriptor().getName());
                    if (existing == null) {
                        hierarchyPropertyDescriptions.put(propertyDescription.getPropertyDescriptor().getName(),
                                propertyDescription);
                    } else if (existing != propertyDescription) {
                        hierarchyPropertyDescriptions.remove(propertyDescription.getPropertyDescriptor().getName());
                        ambigous.add(propertyDescription.getPropertyDescriptor().getName());
                    }
                }
            }
        }
    }

    public Collection<PropertyDescription> getHierarchyPropertyDescriptions() {
        return hierarchyPropertyDescriptions.values();
    }

    public boolean isId(PropertyDescription propertyDescription) {
        for (PropertyDescription pd : identityPropertyDescriptions) {
            if (pd == propertyDescription) {
                return true;
            }
        }
        return false;
    }

    public boolean isInherited(PropertyDescription propertyDescription) {
        return propertyDescription.getClassDescription() != this;
    }

    public Collection<PropertyDescription> getPropertyDescriptions() {
        return propertyDescriptions.values();
    }

    public SchemaDescription getSchemaDescription() {
        return schemaDescription;
    }

    public String getClassName() {
        return className;
    }

    public ClassDescription getSuperClassDescription() {
        return superClassDescription;
    }

    public PropertyDescription getPropertyDescription(String propertyName) {
        return propertyDescriptions.get(propertyName);
    }

    public PropertyDescription getPropertyDescriptionInHierarchy(String propertyName) {
        return hierarchyPropertyDescriptions.get(propertyName);
    }

    public void addPropertyDescription(PropertyDescription propertyDescription) {
        Assert.isNotNull(propertyDescription);
        Assert.isNotNull(propertyDescription.getPropertyDescriptor());
        propertyDescriptions.put(propertyDescription.getPropertyDescriptor().getName(), propertyDescription);
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    public PropertyDescription[] getIdentityPropertyDescriptions() {
        return identityPropertyDescriptions;
    }

    public void setIdentityPropertyDescriptions(PropertyDescription[] identityPropertyDescriptions) {
        this.identityPropertyDescriptions = identityPropertyDescriptions;
    }

    public Class<?> getIdentityClass() {
        return identityClass;
    }

    public void setIdentityClass(Class<?> identityClass) {
        this.identityClass = identityClass;
        identityClassName = identityClass == null ? null : identityClass.getName();
    }

    public Object getIdentity(Object object) {
        Map<String, Object> identityMap = new HashMap<>();
        identityMap.put(TYPE_PROPERTY, getClassName());
        if (identityPropertyDescriptions.length == 0) {
            throw new IllegalStateException();
        } else if (identityPropertyDescriptions.length == 1) {
            identityMap.put(identityPropertyDescriptions[0].getPropertyDescriptor().getName(),
                    ModifiableAccessor.Singleton.getValue(object, identityPropertyDescriptions[0]));
        } else if (identityClass != null) {
            throw new UnsupportedOperationException();
        } else {
            for (PropertyDescription pd : identityPropertyDescriptions) {
                identityMap.put(pd.getPropertyDescriptor().getName(), ModifiableAccessor.Singleton.getValue(object, pd));
            }
        }
        return identityMap;
    }

    public Object getId(Object object) {
        if (identityPropertyDescriptions.length == 0) {
            throw new IllegalStateException();
        } else if (identityPropertyDescriptions.length == 1) {
            return ModifiableAccessor.Singleton.getValue(object, identityPropertyDescriptions[0]);
        } else if (identityClass != null) {
            throw new UnsupportedOperationException();
        } else {
            Map<String, Object> identityMap = new HashMap<>();
            for (PropertyDescription pd : identityPropertyDescriptions) {
                identityMap.put(pd.getPropertyDescriptor().getName(), ModifiableAccessor.Singleton.getValue(object, pd));
            }
            return identityMap;
        }
    }

    @SuppressWarnings("unchecked")
    public void setIdentity(Object object, Object identity) {
        if (identityPropertyDescriptions.length == 0) {
            throw new IllegalStateException();
        } else if (identityPropertyDescriptions.length != 1 && identityClass == null) {
            throw new UnsupportedOperationException();
        } else {
            Map<String, Object> identityMap = (Map<String, Object>) identity;
            for (Map.Entry<String, Object> entry : identityMap.entrySet()) {
                if (!entry.getKey().equals(TYPE_PROPERTY)) {
                    PropertyDescription pd = getPropertyDescription(entry.getKey());
                    ModifiableAccessor.Singleton.setValue(object, pd, entry.getValue());
                }
            }
        }
    }

    public String toString() {
        return getClassName();
    }

    public boolean hasLocalPropertyDescriptions() {
        for (PropertyDescription pd : getPropertyDescriptions()) {
            if (!isInherited(pd)) {
                return true;
            }
        }
        return false;
    }

    public boolean isA(ClassDescription classDescription) {
        if (classDescription == this) {
            return true;
        } else if (classDescription.getSuperClassDescription() == null) {
            return false;
        } else {
            return isA(classDescription.getSuperClassDescription());
        }
    }


}
