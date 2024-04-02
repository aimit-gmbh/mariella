package org.mariella.persistence.util;

public class Assert {

    public static void isNotNull(Object o) {
        assertTrue(o != null, null);
    }

    public static void isNotNull(Object o, String msg) {
        assertTrue(o != null, msg);
    }


    public static void assertTrue(boolean b) {
        assertTrue(b, null);
    }

    public static void assertTrue(boolean b, String msg) {
        if (!b) {
            throw new AssertionError(msg == null ? "Assertion failed" : msg);
        }
    }

}
