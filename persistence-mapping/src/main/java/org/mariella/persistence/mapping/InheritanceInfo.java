package org.mariella.persistence.mapping;

import jakarta.persistence.InheritanceType;


public class InheritanceInfo {

    private InheritanceType strategy;

    public InheritanceType getStrategy() {
        return strategy;
    }

    public void setStrategy(InheritanceType strategy) {
        this.strategy = strategy;
    }


}
