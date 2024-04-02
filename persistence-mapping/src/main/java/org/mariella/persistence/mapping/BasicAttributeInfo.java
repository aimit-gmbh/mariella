package org.mariella.persistence.mapping;

import java.io.PrintStream;
import java.lang.reflect.Type;

public class BasicAttributeInfo extends AttributeInfo {

    private Type type = null;
    private ColumnInfo columnInfo = null;
    private String domainName = null;
    private String converterName = null;
    private GeneratedValueInfo generatedValueInfo = null;
    private boolean isId;
    private boolean isOptionalOrNullable;


    public BasicAttributeInfo() {
    }


    @Override
    public BasicAttributeInfo copyForAdoption() {
        BasicAttributeInfo copy = new BasicAttributeInfo();
        initializeAdoptionCopy(copy);
        return copy;
    }

    @Override
    void initializeAdoptionCopy(AttributeInfo copy) {
        super.initializeAdoptionCopy(copy);
        ((BasicAttributeInfo) copy).type = type;
        ((BasicAttributeInfo) copy).columnInfo = columnInfo;
        ((BasicAttributeInfo) copy).domainName = domainName;
        ((BasicAttributeInfo) copy).generatedValueInfo = generatedValueInfo;
        ((BasicAttributeInfo) copy).converterName = converterName;
        ((BasicAttributeInfo) copy).isId = isId;
        ((BasicAttributeInfo) copy).isOptionalOrNullable = isOptionalOrNullable;
    }

    void debugPrintAttributes(PrintStream out) {
        super.debugPrintAttributes(out);
        if (isId()) out.print(" @id");
        if (converterName != null) {
            out.print(" @converter=");
            out.print(converterName);
        }
        if (columnInfo != null) {
            columnInfo.debugPrint(out);
        }
    }

    void debugPrintTypeInfo(PrintStream out) {
        out.print("<" + ((Class<?>) getType()).getSimpleName() + ">");
    }

    @Override
    public boolean isOptionalOrNullable() {
        return isOptionalOrNullable;
    }

    public void setOptionalOrNullable(boolean isOptionalOrNullable) {
        this.isOptionalOrNullable = isOptionalOrNullable;
    }

    public ColumnInfo getColumnInfo() {
        return columnInfo;
    }

    public void setColumnInfo(ColumnInfo columnInfo) {
        this.columnInfo = columnInfo;
    }

    public GeneratedValueInfo getGeneratedValueInfo() {
        return generatedValueInfo;
    }

    public void setGeneratedValueInfo(GeneratedValueInfo generatedValueInfo) {
        this.generatedValueInfo = generatedValueInfo;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getConverterName() {
        return converterName;
    }

    public void setConverterName(String converterName) {
        this.converterName = converterName;
    }

    @Override
    public void overrideWith(AttributeInfo overriddenAttrInfo) {
        if (columnInfo == null) {
            columnInfo = ((BasicAttributeInfo) overriddenAttrInfo).columnInfo;
        }
        if (domainName == null) {
            domainName = ((BasicAttributeInfo) overriddenAttrInfo).domainName;
        }
        if (generatedValueInfo == null) {
            generatedValueInfo = ((BasicAttributeInfo) overriddenAttrInfo).generatedValueInfo;
        }
        if (converterName == null) {
            converterName = ((BasicAttributeInfo) overriddenAttrInfo).converterName;
        }
    }

    public boolean isId() {
        return isId;
    }

    public void setId(boolean isId) {
        this.isId = isId;
    }

    public Type getType() {
        return type;
    }


    public void setType(Type type) {
        this.type = type;
    }

}
