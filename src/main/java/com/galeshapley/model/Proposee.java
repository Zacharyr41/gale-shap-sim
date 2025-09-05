package com.galeshapley.model;

public class Proposee extends Agent {
    
    public Proposee(String id, String name) {
        super(id, name);
    }
    
    protected Proposee(String id, String name, boolean isEmptySet) {
        super(id, name, isEmptySet);
    }
    
    public static Proposee create(String id, String name) {
        return new Proposee(id, name);
    }
}