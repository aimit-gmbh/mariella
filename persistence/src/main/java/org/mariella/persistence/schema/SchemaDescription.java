package org.mariella.persistence.schema;

import org.mariella.persistence.util.InitializationHelper;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
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
        InitializationHelper<ClassDescription> context = new InitializationHelper<>(ClassDescription::initialize);

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
        InitializationHelper<ClassDescription> context = new InitializationHelper<>(ClassDescription::postInitialize);

        for (ClassDescription classDescription : getClassDescriptions()) {
            context.ensureInitialized(classDescription);
        }

    }

}
