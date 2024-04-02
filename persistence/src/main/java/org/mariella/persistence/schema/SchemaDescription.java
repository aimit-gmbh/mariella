package org.mariella.persistence.schema;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SchemaDescription implements Serializable {
    private final Map<String, ClassDescription> classDescriptions = new HashMap<>();
    private String schemaName;

    public SchemaDescription() {
    }

    public static PropertyDescriptor getPropertyDescriptor(Class<?> beanClass, String propertyName) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                if (pd.getName().equals(propertyName)) {
                    return pd;
                }
            }
        } catch (IntrospectionException ignored) {
        }
        return null;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public ClassDescription getClassDescription(String className) {
        return classDescriptions.get(className);
    }

    public void addClassDescription(ClassDescription classDescription) {
        classDescriptions.put(classDescription.getClassName(), classDescription);
    }

    public Collection<ClassDescription> getClassDescriptions() {
        return classDescriptions.values();
    }

    public void initialize() {
        ClassDescriptionInitializationContext context = new ClassDescriptionInitializationContext() {
            private final Collection<ClassDescription> initialized = new HashSet<>();
            private final Collection<ClassDescription> initializing = new HashSet<>();

            @Override
            public void ensureInitialized(ClassDescription classDescription) {
                if (!initialized.contains(classDescription)) {
                    if (initializing.contains(classDescription)) {
                        throw new IllegalStateException();
                    } else {
                        initializing.add(classDescription);
                        classDescription.initialize(this);
                        initializing.remove(classDescription);
                        initialized.add(classDescription);
                    }
                }

            }
        };

        for (ClassDescription classDescription : getClassDescriptions()) {
            context.ensureInitialized(classDescription);
        }

        postInitialize();
    }

    public void afterDeserialization(ClassLoader classLoader) {
        for (ClassDescription cd : getClassDescriptions()) {
            cd.afterDeserialization(classLoader);
        }
    }

    private void postInitialize() {
        ClassDescriptionInitializationContext context = new ClassDescriptionInitializationContext() {
            private final Collection<ClassDescription> initialized = new HashSet<>();
            private final Collection<ClassDescription> initializing = new HashSet<>();

            @Override
            public void ensureInitialized(ClassDescription classDescription) {
                if (!initialized.contains(classDescription)) {
                    if (initializing.contains(classDescription)) {
                        throw new IllegalStateException();
                    } else {
                        initializing.add(classDescription);
                        classDescription.postInitialize(this);
                        initializing.remove(classDescription);
                        initialized.add(classDescription);
                    }
                }

            }
        };

        for (ClassDescription classDescription : getClassDescriptions()) {
            context.ensureInitialized(classDescription);
        }

    }

}
