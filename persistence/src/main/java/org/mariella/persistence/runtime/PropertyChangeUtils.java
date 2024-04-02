package org.mariella.persistence.runtime;

import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PropertyChangeUtils {

    public static boolean addPropertyChangeListener(Object receiver, PropertyChangeListener listener) {
        try {
            Method method = receiver.getClass()
                    .getMethod("addPropertyChangeListener", PropertyChangeListener.class);
            method.invoke(receiver, listener);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwables.throwIfUnchecked(e.getCause());
            throw new RuntimeException(e.getCause());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean removePropertyChangeListener(Object receiver, PropertyChangeListener listener) {
        try {
            Method method = receiver.getClass()
                    .getMethod("removePropertyChangeListener", PropertyChangeListener.class);
            method.invoke(receiver, listener);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwables.throwIfUnchecked(e.getCause());
            throw new RuntimeException(e.getCause());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
