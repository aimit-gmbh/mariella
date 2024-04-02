package org.mariella.persistence.runtime;

import java.util.HashMap;
import java.util.Map;

public class Introspector {
    public static final Introspector Singleton = new Introspector();

    private final Map<Class<?>, BeanInfo> beanInfos = new HashMap<>();

    protected Introspector() {
        super();
    }

    public BeanInfo getBeanInfo(Class<?> cls) {
        BeanInfo bi = beanInfos.get(cls);
        if (bi == null) {
            synchronized (this) {
                bi = beanInfos.get(cls);
                if (bi == null) {
                    bi = new BeanInfo(cls);
                    beanInfos.put(cls, bi);
                }
            }
        }
        return bi;
    }

}
