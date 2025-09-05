package com.galeshapley.model;

/**
 * Represents the empty set (∅) as a special Proposee.
 * When a Proposer is matched with EmptySet, they prefer being single.
 */
public class EmptySet extends Proposee {
    private static final EmptySet INSTANCE = new EmptySet();
    
    private EmptySet() {
        super("∅", "EmptySet");
    }
    
    public static EmptySet getInstance() {
        return INSTANCE;
    }
    
    @Override
    public String toString() {
        return "∅";
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof EmptySet;
    }
    
    @Override
    public int hashCode() {
        return "EmptySet".hashCode();
    }
}