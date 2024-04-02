package org.mariella.persistence.mapping;


public abstract class ToOneAttributeInfo extends RelationAttributeInfo {

    private boolean isOptionalOrNullable;

    @Override
    public boolean isOptionalOrNullable() {
        return isOptionalOrNullable;
    }

    public void setOptionalOrNullable(boolean isOptionalOrNullable) {
        this.isOptionalOrNullable = isOptionalOrNullable;
    }

}
