package org.mariella.persistence.bootstrap;

public class DefaultClassResolver implements ClassResolver {

    private final ClassLoader classLoader;

    public DefaultClassResolver(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Class<?> resolveClass(String className)
            throws ClassNotFoundException {
        return classLoader.loadClass(className);
    }

}
