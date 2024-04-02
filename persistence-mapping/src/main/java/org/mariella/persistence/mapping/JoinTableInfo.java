package org.mariella.persistence.mapping;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

public class JoinTableInfo {

    private String name;
    private String catalog;
    private String schema;


    @SuppressWarnings("unchecked")
    private List<JoinColumnInfo> joinColumnInfos = Collections.EMPTY_LIST;
    @SuppressWarnings("unchecked")
    private List<JoinColumnInfo> inverseJoinColumnInfos = Collections.EMPTY_LIST;
    @SuppressWarnings("unchecked")
    private List<UniqueConstraintInfo> uniqueConstraintInfos = Collections.EMPTY_LIST;

    public List<JoinColumnInfo> getJoinColumnInfos() {
        return joinColumnInfos;
    }

    public void setJoinColumnInfos(List<JoinColumnInfo> joinColumnInfos) {
        this.joinColumnInfos = joinColumnInfos;
    }

    public List<UniqueConstraintInfo> getUniqueConstraintInfos() {
        return uniqueConstraintInfos;
    }

    public void setUniqueConstraintInfos(List<UniqueConstraintInfo> uniqueConstraintInfos) {
        this.uniqueConstraintInfos = uniqueConstraintInfos;
    }

    public void debugPrint(PrintStream out) {
        out.print(" @JoinTable ");
        out.print(getName());
        for (JoinColumnInfo info : getJoinColumnInfos()) {
            info.debugPrint(out);
        }
        for (UniqueConstraintInfo info : getUniqueConstraintInfos()) {
            info.debugPrint(out);
        }
    }

    public List<JoinColumnInfo> getInverseJoinColumnInfos() {
        return inverseJoinColumnInfos;
    }

    public void setInverseJoinColumnInfos(List<JoinColumnInfo> inverseJoinColumnInfos) {
        this.inverseJoinColumnInfos = inverseJoinColumnInfos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

}
