package org.mariella.persistence.mapping;


public class ManyToManyAttributeInfo extends ToManyAttributeInfo {


    @Override
    public AttributeInfo copyForAdoption() {
        ManyToManyAttributeInfo copy = new ManyToManyAttributeInfo();
        initializeAdoptionCopy(copy);
        return copy;
    }


    @Override
    public boolean isOptionalOrNullable() {
        return true;
    }


}
