package org.mariella.persistence.annotations_processing;

import jakarta.persistence.UniqueConstraint;
import org.mariella.persistence.mapping.UniqueConstraintInfo;

public class UniqueConstraintInfoBuilder {

    private final UniqueConstraint uniqueConstraint;
    private final IModelToDb translator;

    public UniqueConstraintInfoBuilder(UniqueConstraint uniqueConstraint, IModelToDb translator) {
        this.uniqueConstraint = uniqueConstraint;
        this.translator = translator;
    }

    UniqueConstraintInfo buildUniqueConstraintInfo() {
        UniqueConstraintInfo info = new UniqueConstraintInfo();
        String[] names = uniqueConstraint.columnNames();
        for (int i = 0; i < names.length; i++) {
            names[i] = translator.translate(names[i]);
        }
        info.setColumnNames(names);
        return info;
    }

}
