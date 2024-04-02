package org.mariella.persistence.bootstrap;

public interface ClassResolver {

    Class<?> resolveClass(String className) throws ClassNotFoundException;

}
