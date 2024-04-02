package org.mariella.persistence.mapping;

import java.io.PrintStream;

public class EntityListenerClassInfo extends ClassInfo {

    @Override
    public String getName() {
        return getClazz().getName();
    }

    @Override
    public void debugPrint(PrintStream out) {
        out.println(getClazz().getSimpleName() + " (EntityListener)");
        debugPrintLifecycleEventInfos(out);
    }

}
