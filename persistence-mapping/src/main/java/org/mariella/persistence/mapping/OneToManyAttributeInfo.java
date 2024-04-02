package org.mariella.persistence.mapping;


public class OneToManyAttributeInfo extends ToManyAttributeInfo {

    @Override
    public AttributeInfo copyForAdoption() {
        OneToManyAttributeInfo copy = new OneToManyAttributeInfo();
        initializeAdoptionCopy(copy);
        return copy;
    }


    @Override
    public boolean isOptionalOrNullable() {
        return true;
    }

}
