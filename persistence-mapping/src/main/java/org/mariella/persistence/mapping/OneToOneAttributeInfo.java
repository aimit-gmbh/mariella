package org.mariella.persistence.mapping;


public class OneToOneAttributeInfo extends ToOneAttributeInfo {

    @Override
    public AttributeInfo copyForAdoption() {
        OneToOneAttributeInfo copy = new OneToOneAttributeInfo();
        initializeAdoptionCopy(copy);
        return copy;
    }

}
