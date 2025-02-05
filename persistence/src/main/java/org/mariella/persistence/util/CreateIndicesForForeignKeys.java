package org.mariella.persistence.util;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.mapping.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/*
Usage:
CreateIndicesForForeignKeys creator = CreateIndicesForForeignKeys(mySchemaMapping, false);
creator.setIndexTableSpace("ATLAS_INDEX");
creator.execute();
creator.getSqlStatements();
*/
public class CreateIndicesForForeignKeys {

    public final static char[] VOVELS = new char[]{'a', 'e', 'i', 'o', 'u'};
    final Namespace nameSpace = new Namespace();
    final NameAbbreviator nameAbbreviator;
    private final SchemaMapping schemaMapping;
    private final List<Hint> hints = new ArrayList<>();
    private final List<String> sqlStatements = new ArrayList<>();
    private final boolean buildDropStatements;
    String indexTableSpace = null;
    private ClassMapping currentClassMapping;
    private PropertyMapping currentPropertyMapping;
    private Map<Table, Hint> hintMap = null;

    public CreateIndicesForForeignKeys(SchemaMapping schemaMapping, boolean buildDropStatements) {
        this(schemaMapping, buildDropStatements, new NameAbbreviator() {

            @Override
            public String abbreviateTableName(String tableName) {
                return tableName;
            }

            @Override
            public String abbreviateColumnName(String colName) {
                return colName;
            }
        });
    }

    public CreateIndicesForForeignKeys(SchemaMapping schemaMapping, boolean buildDropStatements, NameAbbreviator abbr) {
        super();
        this.schemaMapping = schemaMapping;
        this.buildDropStatements = buildDropStatements;
        this.nameAbbreviator = abbr;
    }

    public void execute() {
        for (ClassMapping classMapping : schemaMapping.getClassMappings()) {
            currentClassMapping = classMapping;
            for (PropertyMapping propertyMapping : classMapping.getPropertyMappings()) {
                currentPropertyMapping = propertyMapping;
                property();
            }
        }

        for (Hint hint : hints) {
            hint.buildIndexName();
        }

        if (buildDropStatements) {
            for (Hint hint : hints) {
                sqlStatements.add(hint.buildDropIndexStatement());
            }
        }

        for (Hint hint : hints) {
            sqlStatements.add(hint.buildCreateIndexStatement());
        }
    }

    private void beginProperty() {
        hintMap = new HashMap<>();
    }

    private void endProperty() {
        for (Map.Entry<Table, Hint> entry : hintMap.entrySet()) {
            if (!entry.getValue().getColumns().isEmpty()) {
                if (!hints.contains(entry.getValue())) {
                    hints.add(entry.getValue());
                }
            }
        }
        hintMap = null;
    }

    public void property() {
        if (currentPropertyMapping instanceof ReferencePropertyMapping) {
            if (((ReferencePropertyMapping) currentPropertyMapping).getJoinColumns() != null) {
                beginProperty();
                for (JoinColumn joinColumn : ((ReferencePropertyMapping) currentPropertyMapping).getJoinColumns()) {
                    addColumn(joinColumn.getMyReadColumn());
                }
                endProperty();
            }
        } else if (currentPropertyMapping instanceof RelationshipAsTablePropertyMapping) {
            beginProperty();
            addColumns(((RelationshipAsTablePropertyMapping) currentPropertyMapping).getForeignKeyMapToOwner().keySet());
            addColumns(((RelationshipAsTablePropertyMapping) currentPropertyMapping).getForeignKeyMapToContent().keySet());
            endProperty();
        }
    }

    private Table getTable(ClassMapping classMapping, Column column) {
        Table table;
        if (classMapping instanceof JoinedClassMapping) {
            table = classMapping.getPrimaryUpdateTable() != null ? classMapping.getPrimaryUpdateTable()
                    : ((JoinedClassMapping) classMapping).getJoinUpdateTable();
        } else {
            table = classMapping.getPrimaryUpdateTable();
        }

        if (table == null) {
            if (classMapping instanceof JoinedClassMapping) {
                table = classMapping.getPrimaryTable() != null ? classMapping.getPrimaryTable()
                        : ((JoinedClassMapping) classMapping).getJoinTable();
            } else {
                table = classMapping.getPrimaryTable();
            }
        }
        if (table == null) {
            return null;
        } else if (table.getColumns().contains(column)) {
            return table;
        } else if (classMapping.getSuperClassMapping() != null) {
            return getTable(classMapping.getSuperClassMapping(), column);
        } else {
            return null;
//		throw new IllegalStateException();
        }
    }

    private void addColumn(Column column) {
        Table table = getTable(currentClassMapping, column);
        if (table != null) {
            Hint hint = hintMap.get(table);
            if (hint == null) {
                hint = new Hint(table);
                hintMap.put(table, hint);
            }
            hint.getColumns().add(column);
        }
    }

    private void addColumns(Collection<Column> columns) {
        for (Column column : columns) {
            addColumn(column);
        }

    }

    public List<String> getSqlStatements() {
        return sqlStatements;
    }

    public String getIndexTableSpace() {
        return indexTableSpace;
    }

    public void setIndexTableSpace(String indexTableSpace) {
        this.indexTableSpace = indexTableSpace;
    }

    public interface NameAbbreviator {
        String abbreviateTableName(String tableName);

        String abbreviateColumnName(String colName);
    }

    static class Namespace {
        final Set<String> usedNames = new HashSet<>();

        public boolean isUsedName(String name) {
            return usedNames.contains(name);
        }

        public void addUsedName(String name) {
            usedNames.add(name);
        }
    }

    private class Hint {
        private final Table table;
        private final List<Column> columns = new ArrayList<>();
        private String indexName;

        public Hint(Table table) {
            super();
            this.table = table;
        }

        @Override
        public int hashCode() {
            return table.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Hint other) || other.table != table) {
                return false;
            }
            if (other.columns.size() != columns.size()) {
                return false;
            }
            for (Column column : columns) {
                if (!other.getColumns().contains(column)) {
                    return false;
                }
            }
            return true;
        }

        public List<Column> getColumns() {
            return columns;
        }

        public String buildDropIndexStatement() {
            return "DROP INDEX " + indexName;
        }

        public String buildCreateIndexStatement() {
            StringWriter w = new StringWriter();
            PrintWriter pw = new PrintWriter(w);
            pw.print("CREATE INDEX " + indexName + " ON " + table.getName() + " (");
            printCommaDelimitedColumnNames(pw);
            pw.print(")");

            if (indexTableSpace != null) {
                pw.print(" TABLESPACE ");
                pw.print(indexTableSpace);
            }

            pw.flush();
            return w.toString();
        }

        private void printCommaDelimitedColumnNames(PrintWriter printWriter) {
            boolean first = true;
            for (Column column : columns) {
                if (first) {
                    first = false;
                } else {
                    printWriter.append(", ");
                }
                printWriter.append(column.name());
            }
            printWriter.flush();
        }

        void buildIndexName() {
            String name = buildBaseName();

            while (name.length() > 30) {
                int len = name.length();
                name = removeLastUnderscore(name);
                if (name.length() == len) {
                    // no more underscores found
                    break;
                }
            }


            while (name.length() > 30) {
                int len = name.length();
                name = removeLastVovel(name);
                if (name.length() == len) {
                    // no more vovels found
                    break;
                }
            }

            if (name.length() > 30) {
                name = name.substring(0, 27);
                int i = 0;
                while (nameSpace.isUsedName(name)) {
                    name = name + i;
                    i++;
                }
            }

            name = name.replace('@', '_');

            // System.out.println(name + " / " + name.length());
            nameSpace.addUsedName(name);
            indexName = name;
        }


        private String removeLastUnderscore(String name) {
            int i = name.lastIndexOf('_');
            if (i >= 0) {
                return name.substring(0, i) + name.substring(i + 1);
            }
            return name;
        }


        private String removeLastVovel(String name) {
            int i = lastIndexOfVovel(name);
            if (i >= 0) {
                return name.substring(0, i) + name.substring(i + 1);
            }
            return name;
        }

        private int lastIndexOfVovel(String name) {
            for (int i = name.length() - 1; i >= 0; i--) {
                if (isVovel(Character.toLowerCase(name.charAt(i))))
                    return i;
            }
            return -1;
        }

        private boolean isVovel(char ch) {
            for (char v : VOVELS)
                if (v == ch)
                    return true;

            return false;
        }

        String buildBaseName() {
            StringBuilder s = new StringBuilder(nameAbbreviator.abbreviateTableName(table.getName()));
            for (Column col : columns) {
                s.append("@").append(nameAbbreviator.abbreviateColumnName(col.name()));
            }
            return s.toString();
        }
    }

}