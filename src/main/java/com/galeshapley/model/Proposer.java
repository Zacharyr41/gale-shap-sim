package com.galeshapley.model;

public class Proposer extends Agent {
    
    public Proposer(String id, String name) {
        super(id, name);
    }
    
    public static Proposer create(String id, String name) {
        return new Proposer(id, name);
    }
}