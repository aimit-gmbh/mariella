package org.mariella.persistence.mapping;

import java.io.PrintStream;


public abstract class AttributeInfo {

    private ClassInfo parentClassInfo;
    private String name = null;
    private AttributeAccessType accessType;

    public ClassInfo getParentClassInfo() {
        return parentClassInfo;
    }

    public void setParentClassInfo(ClassInfo parentClassInfo) {
        this.parentClassInfo = parentClassInfo;
    }

    public abstract AttributeInfo copyForAdoption();

    void initializeAdoptionCopy(AttributeInfo copy) {
        copy.accessType = accessType;
        copy.name = name;
    }

    public void debugPrint(PrintStream out) {
        out.print("\t" + getName());
        out.print("\t-\t" + getClass().getSimpleName());
        debugPrintTypeInfo(out);
        debugPrintAttributes(out);
        out.println();
    }

    void debugPrintAttributes(PrintStream out) {
        if (isOptionalOrNullable())
            out.print(" @optional");
    }

    abstract void debugPrintTypeInfo(PrintStream out);

    // checks #optional, Column#nullable
    public abstract boolean isOptionalOrNullable();

    public String toString() {
        return parentClassInfo.getName() + "." + getName();
    }

    public void overrideWith(AttributeInfo overriddenAttrInfo) {

    }


    public AttributeAccessType getAccessType() {
        return accessType;
    }


    public void setAccessType(AttributeAccessType accessType) {
        this.accessType = accessType;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

}

