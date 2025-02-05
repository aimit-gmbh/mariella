package org.mariella.persistence.util;

public interface InitFunction<T> {
    void init(T classMapping, InitializationHelper<T> initializationHelper);
}
