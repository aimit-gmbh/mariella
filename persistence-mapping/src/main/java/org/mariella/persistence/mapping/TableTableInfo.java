package org.mariella.persistence.mapping;


public class TableTableInfo implements TableInfo {
    private String catalog;
    private String name;
    private String schema;

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return getSchema().equals(((TableTableInfo) obj).getSchema()) && getName().equals(((TableTableInfo) obj).getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

}
