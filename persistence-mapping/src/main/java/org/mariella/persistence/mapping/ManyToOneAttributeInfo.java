package org.mariella.persistence.mapping;


public class ManyToOneAttributeInfo extends ToOneAttributeInfo {

    public ManyToOneAttributeInfo() {
        super();
    }


    @Override
    public AttributeInfo copyForAdoption() {
        ManyToOneAttributeInfo copy = new ManyToOneAttributeInfo();
        initializeAdoptionCopy(copy);
        return copy;
    }


}
