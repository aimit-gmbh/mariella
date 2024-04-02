package org.mariella.persistence.mapping;

import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public abstract class ClassInfo {
    private UnitInfo unitInfo;
    private Class<?> clazz;
    private List<LifecycleEventInfo> lifecycleEventInfos = new ArrayList<>();

    protected void debugPrintLifecycleEventInfos(PrintStream out) {
        if (lifecycleEventInfos.size() == 0) return;

        out.print("\tLifecycleEventInfos: ");
        for (Iterator<LifecycleEventInfo> i = lifecycleEventInfos.iterator(); i.hasNext(); ) {
            LifecycleEventInfo info = i.next();
            out.print(info.getMethod());
            if (i.hasNext())
                out.print(", ");
        }
        out.println();
    }

    public abstract String getName();

    public abstract void debugPrint(PrintStream out);

    public UnitInfo getUnitInfo() {
        return unitInfo;
    }

    public void setUnitInfo(UnitInfo unitInfo) {
        this.unitInfo = unitInfo;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public List<LifecycleEventInfo> getLifecycleEventInfos() {
        return lifecycleEventInfos;
    }

    public void setLifecycleEventInfos(List<LifecycleEventInfo> lifecycleEventInfos) {
        this.lifecycleEventInfos = lifecycleEventInfos;
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(clazz.getModifiers());
    }

}
