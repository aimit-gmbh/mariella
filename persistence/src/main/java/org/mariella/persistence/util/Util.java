package org.mariella.persistence.util;

import java.util.List;

public class Util {

    public static String asStringDelimitedBy(List<String> strings, String delimiter) {
        boolean first = true;
        StringBuilder b = new StringBuilder();
        for (String s : strings) {
            if (first)
                first = false;
            else
                b.append(delimiter);
            b.append(s);
        }
        return b.toString();
    }


    public static void assertTrue(boolean b, String message) {
        if (!b) {
            throw new AssertionError("assertion failed: " + message);
        }
    }
}
