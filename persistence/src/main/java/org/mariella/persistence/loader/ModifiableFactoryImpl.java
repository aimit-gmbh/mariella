package org.mariella.persistence.loader;

import org.mariella.persistence.schema.ClassDescription;


public class ModifiableFactoryImpl implements ModifiableFactory {
    private final ClassLoader classLoader;

    public ModifiableFactoryImpl() {
        super();
        classLoader = null;
    }

    public ModifiableFactoryImpl(ClassLoader classLoader) {
        super();
        this.classLoader = classLoader;
    }

    @Override
    public Class<?> getClass(ClassDescription classDescription) {
        try {
            if (classLoader == null) {
                return Class.forName(classDescription.getClassName());
            } else {
                return classLoader.loadClass(classDescription.getClassName());
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object createModifiable(ClassDescription classDescription) {
        try {
            return getClass(classDescription).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object createEmbeddable(ClassDescription classDescription) {
        return createModifiable(classDescription);
    }

}
