package com.galeshapley.model;

public class Proposee extends Agent {
    
    public Proposee(String id, String name) {
        super(id, name);
    }
    
    public static Proposee create(String id, String name) {
        return new Proposee(id, name);
    }
}