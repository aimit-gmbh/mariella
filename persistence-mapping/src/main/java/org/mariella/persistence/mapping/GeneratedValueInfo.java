package org.mariella.persistence.mapping;

import javax.persistence.GenerationType;


public class GeneratedValueInfo {

    private String generator;
    private GenerationType strategy;

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public GenerationType getStrategy() {
        return strategy;
    }

    public void setStrategy(GenerationType strategy) {
        this.strategy = strategy;
    }


}
