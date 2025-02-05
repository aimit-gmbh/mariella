package org.mariella.persistence.util;

import java.util.Collection;
import java.util.HashSet;

public class InitializationHelper<T> {

    private final InitFunction<T> initFunction;
    private final Collection<T> initialized = new HashSet<>();
    private final Collection<T> initializing = new HashSet<>();

    public InitializationHelper(InitFunction<T> initFunction) {
        this.initFunction = initFunction;
    }

    public void ensureInitialized(T initializer) {
        if (!initialized.contains(initializer)) {
            if (initializing.contains(initializer)) {
                throw new IllegalStateException();
            } else {
                initializing.add(initializer);
                initFunction.init(initializer, this);
                initializing.remove(initializer);
                initialized.add(initializer);
            }
        }

    }
}
