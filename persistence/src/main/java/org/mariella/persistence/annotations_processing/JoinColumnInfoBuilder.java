package org.mariella.persistence.annotations_processing;

import org.mariella.persistence.mapping.JoinColumnInfo;

import javax.persistence.JoinColumn;

public class JoinColumnInfoBuilder {

    private final IModelToDb translator;
    private final JoinColumn joinColumn;

    JoinColumnInfoBuilder(JoinColumn joinColumn, IModelToDb translator) {
        this.joinColumn = joinColumn;
        this.translator = translator;
    }

    public JoinColumnInfo buildJoinColumnInfo() {
        JoinColumnInfo info = new JoinColumnInfo();
        info.setColumnDefinition(joinColumn.columnDefinition());
        info.setInsertable(joinColumn.insertable());
        info.setName(translator.translate(joinColumn.name()));
        info.setNullable(joinColumn.nullable());
        info.setReferencedColumnName(translator.translate(joinColumn.referencedColumnName()));
        info.setUnique(joinColumn.unique());
        info.setUpdatable(joinColumn.updatable());
        return info;
    }


}
