package org.mariella.persistence.mapping;

import java.lang.reflect.Method;

public class LifecycleEventInfo {
    private Method method;
    private LifecycleEventType eventType;

    public LifecycleEventType getEventType() {
        return eventType;
    }

    public void setEventType(LifecycleEventType eventType) {
        this.eventType = eventType;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

}
